//------------------------------------------------------
#include "stdafx.h"
#include "Main.h"

#include "resource.h"

#include "..\common\TracingInterface.h"
#include "..\common\FileHelpers.h"
#include "..\common\TextHelpers.h"
#include "..\common\TimeHelpers.h"

#include "runa.h"

#include <Shlwapi.h>	// for CoInitializeEx
#include <shellapi.h>	// for NOTIFYICONDATA
#include <tlhelp32.h>

//------------------------------------------------------

#undef NOTIFYICONDATAW_V2_SIZE
#define NOTIFYICONDATAW_V2_SIZE RTL_SIZEOF_THROUGH_FIELD(NOTIFYICONDATAW, dwInfoFlags)

//------------------------------------------------------

#define WINDOW_CLASS_NAME		L"RtnLauncherWndClass"
#define PROGRAM_MUTEX_NAME		L"RtnLauncherMutex"

#define WM_CUSTOM_NOTIFY_ICON	(WM_APP + 1)

//------------------------------------------------------

HINSTANCE				g_hInstance = NULL;

// Following variable is used to handle case when explorer.exe is restarted.
// In this case our app will create systray icon again.
// To get more info about TaskbarCreated window message read here:
// http://msdn.microsoft.com/en-us/library/windows/desktop/cc144179%28v=vs.85%29.aspx
UINT					g_uTaskbarRestart = 0;

std::wstring GetErrorMessageBoxTitle()
{
	return RtnResources::GetOption(L"ErrorMessageBoxTitle", L"Error");
}

//------------------------------------------------------

void QuitApplication(HWND hWnd)
{
	DestroyWindow(hWnd);
}

//------------------------------------------------------

bool AddNotifyIcon(HWND hWnd, UINT nResource = IDI_NOTIFICATION_ICON, bool bAdd = true, const std::wstring& sFileName = L"", int status = -1) {
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
	if(!sFileName.empty()) {
		const std::wstring sFullName = GetExeRelativePath(sFileName);
		if(IsFileExists(sFullName))	{
			nid.hIcon = (HICON)LoadImage(NULL, sFullName.c_str(),
				IMAGE_ICON, cx, cy, LR_LOADFROMFILE);
			hLoadedIcon = nid.hIcon;
		}
	}
	if(nid.hIcon == NULL) {
		nid.hIcon = (HICON)LoadImage(g_hInstance, MAKEINTRESOURCE(nResource), IMAGE_ICON, cx, cy, LR_SHARED);
	}
	if(nid.hIcon == NULL) {
		// This one will always load 32x32:
		nid.hIcon = LoadIcon(g_hInstance, MAKEINTRESOURCE(nResource));
	}
	if (!nid.hIcon) {
		return false;
	}
	std::wstring sTooltip;
	if (status == TRUE) {
		sTooltip = RtnResources::GetOption(L"popup.newtasks", L"New tasks found");
	} else if (status == FALSE) {
		sTooltip = RtnResources::GetOption(L"popup.no.tasks", L"No tasks found");
	} else {
		sTooltip = RtnResources::GetOption(L"popup.error", L"Error");
	}
	
	wcscpy_s(nid.szTip, sTooltip.c_str());
	const BOOL res = Shell_NotifyIcon(bAdd ? NIM_ADD : NIM_MODIFY, &nid);
	if(hLoadedIcon) {
		DestroyIcon(hLoadedIcon);
		hLoadedIcon = NULL;
	}
	return !!res;
}

//------------------------------------------------------

void UpdateIconStatus(HWND hWnd) {
	const int res = IsThereAnyTask();
	if (res == TRUE) {
		AddNotifyIcon(hWnd, IDI_ICON_FOUND, false, L"newtasks.ico", res);
	} else if (res == FALSE) {
		AddNotifyIcon(hWnd, IDI_ICON_NOT_FOUND, false, L"no.newtasks.ico", res);
	} else {
		AddNotifyIcon(hWnd, IDI_ICON_ERROR, false, L"error.ico", res);
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

void SetItemText(HMENU hMenu, int nID, const std::wstring& sText)
{
	ModifyMenu(hMenu, nID, MF_BYCOMMAND | MF_STRING, nID, sText.c_str());
}

//------------------------------------------------------

void PrepareContextMenu(HMENU hMenu) {
	SetItemText(hMenu, ID__BROWSE, RtnResources::GetOption(L"menu.open", L"Open"));
	SetItemText(hMenu, ID__SETTINGS, RtnResources::GetOption(L"menu.update", L"Update status"));
	SetItemText(hMenu, ID__QUIT, RtnResources::GetOption(L"menu.exit", L"Quit"));
}

void ShowContextMenu(HWND hWnd, POINT pt) {
	HMENU hMenu = LoadMenu(g_hInstance, MAKEINTRESOURCE(IDR_MENU_CONTEXT));
	if (hMenu) {
		HMENU hSubMenu = GetSubMenu(hMenu, 0);
		if (hSubMenu) {
			// our window must be foreground before calling TrackPopupMenu or the menu will not disappear when the user clicks away
			SetForegroundWindow(hWnd);
			PrepareContextMenu(hSubMenu);
			// respect menu drop alignment
			UINT uFlags = TPM_RIGHTBUTTON;
			if (GetSystemMetrics(SM_MENUDROPALIGNMENT) != 0) {
				uFlags |= TPM_RIGHTALIGN;
			} else {
				uFlags |= TPM_LEFTALIGN;
			}
			TrackPopupMenuEx(hSubMenu, uFlags, pt.x, pt.y, hWnd, NULL);
		} else {
			MessageBox(hWnd, L"Error loading submenu.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);
		}
		DestroyMenu(hMenu);
	} else {
		MessageBox(hWnd, L"Error loading menu.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);
	}
}

void OnTimer(HWND hWnd) {
	UpdateIconStatus(hWnd);
}

void Launch(HWND hWnd) {
	// use empty string to use default system browser, L"IEXPLORE.EXE" for MSIE:
	const std::wstring sCmd = RtnResources::GetOption(L"browser.command", L"");
	const int nShowCmd = RtnResources::GetOptionInt(L"browser.command.show", SW_SHOWMAXIMIZED);
	ShellExecute(hWnd, L"open", sCmd.c_str(), RtnResources::GetBrowserStartURL().c_str(), NULL, nShowCmd);
}

bool InitializeSystrayIcon(HWND hWnd) {
	if (!AddNotifyIcon(hWnd)) {
		return false;
	}
	UpdateIconStatus(hWnd);
	return true;
}

LRESULT CALLBACK WindowProcMain(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
	switch (uMsg) {
	case WM_CREATE: {
		// standard Windows message. Read here: http://msdn.microsoft.com/en-us/library/windows/desktop/cc144179%28v=vs.85%29.aspx
		g_uTaskbarRestart = RegisterWindowMessage(L"TaskbarCreated"); 
		const bool bInitOK = InitializeSystrayIcon(hWnd);
		if(!bInitOK) {
			MessageBox(NULL, L"Error adding notification icon.", GetErrorMessageBoxTitle().c_str(), MB_OK | MB_ICONERROR);
			QuitApplication(hWnd);
			return -1;
		}
		const int nTimerRes = RtnResources::GetOptionInt(L"check.tasks.timeout", 60) * 1000;
		SetTimer(hWnd, 1, max(3000, nTimerRes), NULL);
		}
		break;
	case WM_TIMER:
		OnTimer(hWnd);
		break;
	case WM_CUSTOM_NOTIFY_ICON:
		switch (LOWORD(lParam)) {
		case WM_LBUTTONDBLCLK:
			Launch(hWnd);
			break;
		case WM_RBUTTONUP:
			POINT pt;
			GetCursorPos(&pt);
			ShowContextMenu(hWnd, pt);
			break;
		}
		break;
	case WM_COMMAND:
		switch (LOWORD(wParam)) {
		case ID__QUIT:
			QuitApplication(hWnd);
			break;
		case ID__SETTINGS:
			UpdateIconStatus(hWnd);
			break;
		case ID__BROWSE:
			Launch(hWnd);
			break;
		}
		break;
	case WM_DESTROY:
		if (!DeleteNotifyIcon(hWnd)) {
			MessageBox(NULL, L"Error deleting notification icon.", L"ERROR", MB_OK | MB_ICONERROR);
			return -1;
		}
		PostQuitMessage(0);
		break;
	case WM_CLOSE:
		DestroyWindow(hWnd);
		break;
	default:
		// standard Windows message. Read here: http://msdn.microsoft.com/en-us/library/windows/desktop/cc144179%28v=vs.85%29.aspx
		if(uMsg == g_uTaskbarRestart) {
			InitializeSystrayIcon(hWnd);
		}
		return DefWindowProc(hWnd, uMsg, wParam, lParam);
	}
	return 0;
}

BOOL RegisterMainWindowClass() {
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
	if (!RegisterClassEx(&wc)) {
		return false;
	}
	return true;
}

bool g_bDeveloperMode = true;

bool OnAssertMessageBox(LPCSTR szMessage, LPVOID pArg, const bool bBeforeMessageBox, const bool bShortAssert, const CTracer::TRACE_REASON reason) {
	if(bBeforeMessageBox) {
		if(g_bDeveloperMode) {
			return true;
		}
	}
	// Skip ASSERT messagebox in non-developer mode!
	return false;
}

bool WriteTracerAdditional(LPVOID pAdditionalTracerArg,	LPCVOID pData, const int nBytes) {
	// binary data, text is CRLF-formatted before
	const std::wstring sLogFile = RtnResources::GetLogFile();
	if(!sLogFile.empty()) {
		const std::string sMessage = GetStringDateTime() + "\t"	+ std::string((LPCSTR)pData, nBytes) + "\r\n"; 
		CMyFile f;
		f.AppendTextWithOpeningFile(sLogFile, sMessage);
		f.close();
	}
	return false; // don't write to console
}

int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR pCmdLine, int nCmdShow) {
	g_hInstance = hInstance;
	SetAdditionalTracer(WriteTracerAdditional, NULL);
	SetAssertMessageBoxHandler(OnAssertMessageBox, NULL);
	MYTRACE("===============================================================================\n");
	// Check if this is a first copy of program in current Windows Terminal session:
	SetLastError(0);
	HANDLE hSingleObject = ::CreateMutex(NULL, FALSE, L"Local\\" PROGRAM_MUTEX_NAME);
	if (hSingleObject && GetLastError() == ERROR_ALREADY_EXISTS) {
		MYTRACE("Process is already running!\n");
		return 1;
	}
	if (!hSingleObject) {
		MYTRACE("Can't create mutex! Error = %d\n", GetLastError());
		return 1;
	}
	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE); // for ShellExecute()
	if (FAILED(hr))	{
		const std::wstring sMsg = StdString::FormatW(L"Failed to initialize COM library. Error code = 0x%08X", hr);
		MYTRACE("%ls\n", sMsg.c_str());
		MessageBoxW(NULL, sMsg.c_str(), GetErrorMessageBoxTitle().c_str(), MB_OK|MB_ICONERROR);
		return 1;
	}
	// get window name
	const std::wstring sWindowName = RtnResources::GetOption(L"application.name", L"RunaWFE tasks notifier");
	MSG msg;
	ZeroMemory(&msg, sizeof(msg));
	if (!RegisterMainWindowClass()) {
		return 1;
	}
	DWORD dwStyle = WS_POPUPWINDOW|WS_CAPTION|WS_MINIMIZEBOX|WS_VISIBLE;
#if 1
	dwStyle = 0;
#endif

	HWND hWnd = CreateWindowW(WINDOW_CLASS_NAME, sWindowName.c_str(), dwStyle, 100, 100, 200, 200, NULL, NULL, g_hInstance, NULL);
	if (hWnd != NULL) {
		BOOL bRet = FALSE;
		while ((bRet = GetMessageW(&msg, NULL, 0, 0))) {
			if (bRet == -1) {
				// handle the error and possibly exit
			} else {
				TranslateMessage(&msg);
				DispatchMessageW(&msg);
			}
		}
	} else {
		DWORD error = GetLastError();
		int i = 0;
	}
	CoUninitialize();
	CloseHandle(hSingleObject);
	return 0;
}