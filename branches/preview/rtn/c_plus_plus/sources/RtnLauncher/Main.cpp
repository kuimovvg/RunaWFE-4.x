//------------------------------------------------------
#include "stdafx.h"

//#include <Windows.h>
#include <Shlwapi.h>

#include "resource.h"

#include "Utils.h"

#include "..\common\TracingInterface.h"
#include "..\common\FileHelpers.h"
#include "..\common\TextHelpers.h"

#include "..\common\network\WinHttpWrapper.h"

#include <Shlobj.h>
#pragma comment(lib, "shell32.lib")

#include <tlhelp32.h>

#undef NOTIFYICONDATAW_V2_SIZE
#define NOTIFYICONDATAW_V2_SIZE RTL_SIZEOF_THROUGH_FIELD(NOTIFYICONDATAW, dwInfoFlags)

//------------------------------------------------------

#define WINDOW_CLASS_NAME		L"RtnLauncherWndClass"

#define WM_CUSTOM_NOTIFY_ICON	(WM_APP + 1)

//------------------------------------------------------

HINSTANCE				g_hInstance = NULL;

using namespace std;

//------------------------------------------------------

inline wstring GetOption(const std::wstring& sStringName, const std::wstring& sDefaultValue = L"")
{
	Config cfg(GetExeFolder() + GetExeName() + L".ini");
	return cfg.GetString(L"options", sStringName, sDefaultValue);
}

//------------------------------------------------------

inline int GetOptionInt(const std::wstring& sStringName, const int nDefaultValue = 0)
{
	Config cfg(GetExeFolder() + GetExeName() + L".ini");
	return cfg.GetInt(L"options", sStringName, nDefaultValue);
}

//------------------------------------------------------

inline wstring MyLoadString(const std::wstring& sStringName, const std::wstring& sDefaultValue = L"")
{
	return sDefaultValue;
}

//------------------------------------------------------

std::wstring GetErrorMessageBoxTitle()
{
	return MyLoadString(L"ErrorMessageBoxTitle", GetOption(L"ErrorMessageBoxTitle", L"Rtn Launcher"));
}

//------------------------------------------------------

bool IsProcNameAlreadyRunning(const wchar_t* exeFile, DWORD currentProcId)
{
	DWORD procId = 0;
	bool isAlreadyRunning = false;

	if (exeFile == NULL)
	{
		return isAlreadyRunning;
	}

	HANDLE h = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

	PROCESSENTRY32 pe = { 0 };
	pe.dwSize = sizeof(PROCESSENTRY32);

	if (Process32First(h, &pe))
	{
		do
		{
			if (_wcsicmp(pe.szExeFile, exeFile) == 0 && currentProcId != pe.th32ProcessID)
			{
				isAlreadyRunning = true;
				break;
			}
		}
		while(Process32Next(h, &pe));
	}
	CloseHandle(h);

	return isAlreadyRunning;
}

//------------------------------------------------------

int IsThereAnyTask()
{
	HttpSessionPtr ptrSession = HttpSession::Create();
	if(!ptrSession)
		return -1;

	std::string sBinaryResponce;

	{
		REQUEST_INFO request(VERB_POST, GetOption(L"ServerUrl", L"http://localhost:8080/") + std::wstring(L"wfe/login.do"));
		request.sBinaryRequestBody =
			StdString::Format("login=%ls&password=%ls",
			GetOption(L"ServerLogin", L"Administrator").c_str(),
			GetOption(L"ServerPassword", L"wf").c_str());
		request.sContentType = L"application/x-www-form-urlencoded";
		REQUEST_RESULTS results;

		const bool bRes = ptrSession->DownloadFromUrl(request, results, false, 3);
		if(!bRes)
			return -1;

		const REQUEST_RESULT& res = results.GetLastResult();
		sBinaryResponce = res.sBinaryReplyBody;

		if(!contains(res.request.sWholeUrl, L"/wfe/manage_tasks.do"))
			return -1; // bad redirect after login
	}

	if(!contains(sBinaryResponce, "/wfe/logout.do"))
		return -1; // can't login

	// http://localhost:8080/wfe/manage_tasks.do?tabForwardName=manage_tasks
// 	if(contains(sBinaryResponce, "/wfe/submitTaskDispatcher.do"))
// 	{
// 	}
	if(contains(sBinaryResponce, "<tr style='font-weight: bold;'><td class='list'>"))
	{
		return TRUE; // found some tasks
	}

	return FALSE;
}

//------------------------------------------------------

void QuitApplication(HWND hWnd)
{
	DestroyWindow(hWnd);
}

//------------------------------------------------------

BOOL AddNotifyIcon(HWND hWnd,
	UINT nResource = IDI_NOTIFICATION_ICON,
	bool bAdd = true,
	const std::wstring& sFileName = L"")
{
	HICON hLoadedIcon = NULL;

	NOTIFYICONDATAW nid;
	ZeroMemory(&nid, sizeof(nid));

	nid.cbSize = NOTIFYICONDATAW_V2_SIZE;
	MYASSERT(nid.cbSize <= sizeof(nid));
	nid.hWnd = hWnd;
	nid.uID = 1;
	nid.uFlags = NIF_ICON | NIF_MESSAGE | NIF_TIP;
	nid.uCallbackMessage = WM_CUSTOM_NOTIFY_ICON;

	// Load 16x16 icon (or other size reported by windows):
	const int cx = GetSystemMetrics(SM_CXSMICON);
	const int cy = GetSystemMetrics(SM_CYSMICON);

	if(!sFileName.empty())
	{
		const std::wstring sFullName = GetExeRelativePath(sFileName);
		if(IsFileExists(sFullName))
		{
			nid.hIcon = (HICON)LoadImage(NULL, sFullName.c_str(),
				IMAGE_ICON, cx, cy, LR_LOADFROMFILE);
			hLoadedIcon = nid.hIcon;
		}
	}
	if(nid.hIcon == NULL)
	{
		nid.hIcon = (HICON)LoadImage(g_hInstance, MAKEINTRESOURCE(nResource),
			IMAGE_ICON, cx, cy, LR_SHARED);
	}
	if(nid.hIcon == NULL)
	{
		// This one will always load 32x32:
		nid.hIcon = LoadIcon(g_hInstance, MAKEINTRESOURCE(nResource));
	}
	if (!nid.hIcon)
		return false;

	const std::wstring sTooltip = MyLoadString(L"SystrayTooltip", GetErrorMessageBoxTitle().c_str());
	wcscpy_s(nid.szTip, sTooltip.c_str());

	const BOOL res = Shell_NotifyIcon(bAdd ? NIM_ADD : NIM_MODIFY, &nid);

	if(hLoadedIcon)
	{
		DestroyIcon(hLoadedIcon);
		hLoadedIcon = NULL;
	}

	return res;
}

//------------------------------------------------------

void UpdateIconStatus(HWND hWnd)
{
	const int res = IsThereAnyTask();
	if(res == TRUE)
	{
		AddNotifyIcon(hWnd, IDI_ICON_FOUND, false, GetOption(L"icon_found", L"Found.ico"));
	}
	else if(res == FALSE)
	{
		AddNotifyIcon(hWnd, IDI_ICON_NOT_FOUND, false, GetOption(L"icon_not_found", L"NotFound.ico"));
	}
	else
	{
		AddNotifyIcon(hWnd, IDI_ICON_ERROR, false, GetOption(L"icon_error", L"Error.ico"));
	}
}

//------------------------------------------------------

BOOL DeleteNotifyIcon(HWND hWnd)
{
	NOTIFYICONDATAW nid;
	ZeroMemory(&nid, sizeof(nid));

	nid.cbSize = sizeof(NOTIFYICONDATAW);
	nid.hWnd = hWnd;
	nid.uID = 1;

	return Shell_NotifyIcon(NIM_DELETE, &nid);
}

//------------------------------------------------------

void ShowContextMenu(HWND hWnd, POINT pt)
{
	HMENU hMenu = LoadMenu(g_hInstance, MAKEINTRESOURCE(IDR_MENU_CONTEXT));
	if (hMenu)
	{
		HMENU hSubMenu = GetSubMenu(hMenu, 0);
		if (hSubMenu)
		{
			// our window must be foreground before calling TrackPopupMenu or the menu will not disappear when the user clicks away
			SetForegroundWindow(hWnd);

			// respect menu drop alignment
			UINT uFlags = TPM_RIGHTBUTTON;
			if (GetSystemMetrics(SM_MENUDROPALIGNMENT) != 0)
				uFlags |= TPM_RIGHTALIGN;
			else
				uFlags |= TPM_LEFTALIGN;

			TrackPopupMenuEx(hSubMenu, uFlags, pt.x, pt.y, hWnd, NULL);
		} // if
		else
			MessageBox(hWnd, L"Error loading submenu.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);

		DestroyMenu(hMenu);
	} // if
	else
		MessageBox(hWnd, L"Error loading menu.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);

} // ShowContextMenu();

//------------------------------------------------------

void OnTimer(HWND hWnd)
{
	DWORD dwRet = 0;

	//--------------------------

	UpdateIconStatus(hWnd);

	//--------------------------

} // OnTimer();

//------------------------------------------------------

void Launch(HWND hWnd)
{
	const std::wstring sCmd = L"IEXPLORE.EXE";
	const std::wstring sParam = GetOption(L"LaunchURL", L"http://google.com/");
	ShellExecute(hWnd, L"open", sCmd.c_str(), sParam.c_str(), NULL, GetOptionInt(L"LaunchURL_ShowCmd", SW_SHOWMAXIMIZED));
}

//------------------------------------------------------

LRESULT CALLBACK WindowProcMain(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch (uMsg)
	{
		case WM_CREATE:
			// add the notification icon
			if (!AddNotifyIcon(hWnd))
			{
				MessageBox(hWnd, L"Error adding notification icon.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);
				return -1;
			} // if
			{
				UpdateIconStatus(hWnd);
				const int nTimerRes = GetOptionInt(L"CheckPeriod", 60) * 1000;
				SetTimer(hWnd, 1, max(3000, nTimerRes), NULL);
			}
			break; // WM_CREATE

		case WM_TIMER:
			OnTimer(hWnd);
			break;

		case WM_CUSTOM_NOTIFY_ICON:
			switch (LOWORD(lParam))
			{
				case WM_LBUTTONDBLCLK:
					Launch(hWnd);
					break;

//				case WM_CONTEXTMENU:
				case WM_RBUTTONUP:
//					POINT pt = { GET_X_LPARAM(wParam), GET_Y_LPARAM(wParam) };
					POINT pt;
					GetCursorPos(&pt);
					ShowContextMenu(hWnd, pt);
					break;
			} // switch
			break; // WM_NOTIFY_ICON

		case WM_COMMAND:
			switch (LOWORD(wParam))
			{
				case ID__QUIT:
					QuitApplication(hWnd);
					break;

				case ID__SETTINGS:
					UpdateIconStatus(hWnd);
					break;

				case ID__BROWSE:
					Launch(hWnd);
					break;

			} // switch
			break; // WM_COMMAND

		case WM_DESTROY:
			if (!DeleteNotifyIcon(hWnd))
			{
				MessageBox(hWnd, L"Error deleting notification icon.", L"ERROR", MB_OK | MB_ICONERROR);
				return -1;
			} // if

			PostQuitMessage(0);
			break; // WM_DESTROY

		case WM_CLOSE:
			DestroyWindow(hWnd);
			break;

		default:
			return DefWindowProc(hWnd, uMsg, wParam, lParam);
	} // switch

	return 0;
} // WindowProcMain();

//------------------------------------------------------

BOOL RegisterMainWindowClass()
{
	WNDCLASSEXW wc;
	ZeroMemory(&wc, sizeof(wc));

	wc.cbSize = sizeof(WNDCLASSEX);
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;
	wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.hIcon = LoadIcon(NULL, MAKEINTRESOURCE(IDI_NOTIFICATION_ICON));
	wc.hIconSm = LoadIcon(NULL, MAKEINTRESOURCE(IDI_NOTIFICATION_ICON));
	wc.hInstance = g_hInstance;
	wc.lpfnWndProc = WindowProcMain;
	wc.lpszClassName = WINDOW_CLASS_NAME;
	wc.lpszMenuName = NULL;
	wc.style = CS_HREDRAW | CS_VREDRAW;

	if (!RegisterClassEx(&wc))
		return false;

	return true;
} // RegisterMainWindowClass();

//------------------------------------------------------

bool g_bDeveloperMode = true;

//------------------------------------------------------

bool OnAssertMessageBox(LPCSTR szMessage, LPVOID pArg,
	const bool bBeforeMessageBox, const bool bShortAssert,
	const CTracer::TRACE_REASON reason) // returns true to execute default handler next
{
	if(bBeforeMessageBox)
	{
		if(g_bDeveloperMode)
		{
			return true;
		}
	}
	// Skip ASSERT messagebox in non-developer mode!
	return false;
}

//------------------------------------------------------

bool WriteTracerAdditional(LPVOID pAdditionalTracerArg,
	LPCVOID pData, const int nBytes)  // binary data, text is CRLF-formatted before
{
	return false; // don't write to console
}

//------------------------------------------------------

int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR pCmdLine, int nCmdShow)
{
	g_hInstance = hInstance;

	SetAdditionalTracer(WriteTracerAdditional, NULL);
	SetAssertMessageBoxHandler(OnAssertMessageBox, NULL);

	if (IsProcNameAlreadyRunning(GetExeFullName().c_str(), GetCurrentProcessId()))
	{
		return 1;
	}

	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE); // for ShellExecute()
	if (FAILED(hr))
	{
		wchar_t buf[200] = L"";
		swprintf_s(buf, L"Failed to initialize COM library. Error code = 0x%08X", hr);
		MessageBoxW(NULL, buf, GetErrorMessageBoxTitle().c_str(), MB_OK|MB_ICONERROR);
		return 1;
	}

	// get window name
	const std::wstring sWindowName = MyLoadString(L"SystrayTooltip", GetOption(L"SystrayTooltip", L"Rtn Launcher"));

	MSG msg;
	ZeroMemory(&msg, sizeof(msg));

	if (!RegisterMainWindowClass())
		return 1;

	DWORD dwStyle = WS_POPUPWINDOW|WS_CAPTION|WS_MINIMIZEBOX|WS_VISIBLE;
#if 1
	dwStyle = 0;
#endif

	HWND hWnd = CreateWindowW(WINDOW_CLASS_NAME, sWindowName.c_str(), dwStyle, 100, 100, 200, 200, NULL, NULL, g_hInstance, NULL);

	if (hWnd != NULL)
	{
		BOOL bRet = FALSE;
		while ((bRet = GetMessageW(&msg, NULL, 0, 0)))
		{
			if (bRet == -1)
			{
				// handle the error and possibly exit
			}
			else
			{
				TranslateMessage(&msg);
				DispatchMessageW(&msg);
			}
		} // while();
	} // if
	else
	{
		DWORD error = GetLastError();
		int i = 0;
	} // else

	CoUninitialize();

	return (int)msg.wParam;
} // wWinMain();

//------------------------------------------------------
