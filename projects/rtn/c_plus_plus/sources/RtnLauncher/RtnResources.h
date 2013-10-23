#pragma once

#include <string>
#include "..\common\FileHelpers.h"
#include "..\common\TextHelpers.h"

class RtnResources
{
public:
	RtnResources(void);
	~RtnResources(void);
	static std::wstring GetOption(const std::wstring& propertyName, const std::wstring& defaultValue);
	static int GetOptionInt(const std::wstring& propertyName, const int defaultValue);
	static std::wstring GetWebServiceURL(std::wstring serviceName);
	static std::wstring GetBrowserStartURL();
	static std::wstring GetLogFile();
private: 
	static Config config;
};

