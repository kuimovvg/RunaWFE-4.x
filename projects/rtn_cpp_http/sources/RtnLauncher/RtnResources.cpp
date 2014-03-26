#include "StdAfx.h"
#include "RtnResources.h"

Config RtnResources::config(GetExeFolder() + L"application.properties");

RtnResources::RtnResources(void) {
}


RtnResources::~RtnResources(void)
{
}

std::wstring RtnResources::GetOption(const std::wstring& propertyName, const std::wstring& defaultValue) {
	return Utf8ToUnicode(config.GetStringA(L"options", propertyName, UnicodeToUtf8(defaultValue))); // UTF8 codepage
}

int RtnResources::GetOptionInt(const std::wstring& propertyName, const int defaultValue) {
	return config.GetInt(L"options", propertyName, defaultValue);
}

std::wstring RtnResources::GetWebServiceURL(std::wstring serviceName) {
	const std::wstring serverName = GetOption(L"server.name", L"localhost");
	const std::wstring serverPort = GetOption(L"server.port", L"8080");
	const std::wstring serverVersion = GetOption(L"server.version", L"4.0.6");
	const std::wstring serverType = GetOption(L"application.server.type", L"jboss4");
	if (L"jboss4" == serverType) {
		return L"http://" + serverName + L":" + serverPort + L"/runawfe-wfe-service-" + serverVersion + L"/" + serviceName + L"ServiceBean?wsdl";
	} else {
		return L"http://" + serverName + L":" + serverPort + L"/wfe-service-" + serverVersion + L"/" + serviceName + L"WebService/" + serviceName + L"API?wsdl";
	}
}

std::wstring RtnResources::GetBrowserStartURL() {
	const std::wstring serverName = RtnResources::GetOption(L"server.name", L"localhost");
	const std::wstring serverPort = RtnResources::GetOption(L"server.port", L"8080");
	const std::wstring loginRelativeURL = RtnResources::GetOption(L"login.relative.url", L"/login.do");
	return L"http://" + serverName + L":" + serverPort + L"/wfe" + loginRelativeURL;
}

std::wstring RtnResources::GetLogFile() {
	return GetOption(L"debug.log.file", L"");
}