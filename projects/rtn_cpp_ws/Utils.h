#pragma once
#include <string>
#include "RtnResources.h"
#include "Connector.h"
#define WM_CUSTOM_NOTIFY_ICON	(WM_APP + 1)
using namespace std;

namespace Auth {
	string GetKerberosTicket(const wstring& sKerberosTargetName);
}

namespace IO {
	wstring ToWideString(const string& string);
	string ToString(const wstring& string);
	wstring GetFilePath(const wstring& relativeFileName);
	wstring GetTemporaryFilePath(const wstring& relativeFileName);
	wstring GetVersionByUrl(const string& url);
}

namespace UI {
	void QuitApplication(HWND hWnd, Server::Connector* connector, const char* cause);
	void LaunchBrowser(HWND hWnd);
	void SetMenuLabel(HMENU hMenu, int nID, const wstring& label);
	bool UpdateNotificationIcon(HWND hWnd, Server::Connector* connector, bool modeAdd = false);
	int DeleteNotificationIcon(HWND hWnd);
}
