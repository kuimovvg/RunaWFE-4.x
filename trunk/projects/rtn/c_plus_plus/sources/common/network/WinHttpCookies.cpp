// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#include "stdafx.h"
#include "WinHttpCookies.h"

#include "..\..\common\TextHelpers.h"
#include "..\..\common\TimeHelpers.h"
#include "..\..\common\array_ptr.h"

#include <algorithm>

bool MyWinHttpTimeToSystemTime(const std::wstring& sTimeStamp1, LPSYSTEMTIME pst);
std::string MyWinHttpDecodeHttpEncodedChars(const std::string& sText); // Decode %XX patterns, output is UTF8
std::string MyWinHttpEncodeHttpEncodedChars(const std::string& sText); // Encode %XX patterns, input is UTF8

#ifdef UNITTEST_DEPENDENCY
#include "..\..\common\UnitTest\UnitTest.h"
IMPLEMENT_MODULE(WinHttpCookiesSuite)
#endif

//------------------------------------------------------

HttpCookiePath::iterator HttpCookiePath::GetCookie(const std::wstring& sCookieName) // ignore case
{
	const std::wstring sLowerCookieName = ToLower(sCookieName);
	HttpCookiePath::iterator iter;
	for(iter = begin(); iter != end(); iter++)
	{
		const HttpCookie& cookie = *iter;
		if(sLowerCookieName == ToLower(cookie.m_sName))
		{
			return iter;
		}
	}
	return end();
}

//------------------------------------------------------

// RFC 2616                        HTTP/1.1                       June 1999
// 
// 3.2.3 URI Comparison
// 
//    When comparing two URIs to decide if they match or not, a client
//    SHOULD use a case-sensitive octet-by-octet comparison of the entire
//    URIs, with these exceptions:
// 
//       - A port that is empty or not given is equivalent to the default
//         port for that URI-reference;
// 
//         - Comparisons of host names MUST be case-insensitive;
// 
//         - Comparisons of scheme names MUST be case-insensitive;
// 
//         - An empty abs_path is equivalent to an abs_path of "/".
// 
//    Characters other than those in the "reserved" and "unsafe" sets (see
//    RFC 2396 [42]) are equivalent to their ""%" HEX HEX" encoding.
// 
//    For example, the following three URIs are equivalent:
// 
//       http://abc.com:80/~smith/home.html
//       http://ABC.com/%7Esmith/home.html
//       http://ABC.com:/%7esmith/home.html
//  

//------------------------------------------------------

std::wstring HttpCookieNameValueList::FormatHttpCookieString() const
{
	// Cookie: <name>=<value> [;<name>=<value>]...

	std::wstring res;
	HttpCookieNameValueList::const_iterator iter;
	for(iter = begin(); iter != end(); iter++)
	{
		const std::wstring& sName = iter->first;
		const std::wstring& sValue = iter->second;
		res += StdString::FormatW(L" %s=%s;", sName.c_str(), sValue.c_str());
	}
	if(!res.empty())
	{
		res = L"Cookie:" + res.substr(0, res.length()-1); // Add header name and remove last ';'
	}
	return res;
}

//------------------------------------------------------

bool HttpCookieNameValueList::Exists(const std::wstring& sCookieName) const // ignore case
{
	const std::wstring sLowerCookieName = ToLower(sCookieName);
	HttpCookieNameValueList::const_iterator iter;
	for(iter = begin(); iter != end(); iter++)
	{
		const std::wstring& sName = ToLower(iter->first);
		if(sName == sLowerCookieName)
		{
			return true;
		}
	}
	return false;	
}

//------------------------------------------------------

std::wstring HttpCookieNameValueList::GetCookieValue(const std::wstring& sCookieName) const // ignore case
{
	const std::wstring sLowerCookieName = ToLower(sCookieName);
	HttpCookieNameValueList::const_iterator iter;
	for(iter = begin(); iter != end(); iter++)
	{
		const std::wstring& sName = ToLower(iter->first);
		const std::wstring& sValue = iter->second;
		if(sName == sLowerCookieName)
		{
			return sValue;
		}
	}
	return L"";	
}

//------------------------------------------------------

HttpCookieNameValueList HttpCookiePathVector::GetNameValueList() const
{
	HttpCookieNameValueList m;
	HttpCookiePathVector::const_iterator iter;
	for(iter = begin(); iter != end(); iter++)
	{
		const HttpCookie& cookie = *iter;
		m.push_back(std::make_pair(cookie.m_sName, cookie.m_sRawHttpValue));
	}
	return m;
}

//------------------------------------------------------

inline std::wstring Domain2AllSubDomains(const std::wstring& sDomain)
{
	// Changes "domain.com" -> ".domain.com" which makes such domain to catch all subdomains.
	if(sDomain.empty())
		return L".";
	const WCHAR chFirst = sDomain[0];
	if(chFirst != L'.')
	{
		return L"." + sDomain;
	}
	return sDomain;
}

//------------------------------------------------------

inline std::wstring Path2AllSubFolders(const std::wstring& sPathOrig)
{
	// Changes "/dir/path" -> "/dir/path/" which makes such path to catch all paths beginning from "/dir/path/".
	if(sPathOrig.empty())
		return L"/";

	std::wstring sPath;
	const size_t pos = sPathOrig.find_first_of(L"?#");
	if(pos != std::wstring::npos)
	{
		sPath = sPathOrig.substr(0, pos);
	}
	else
	{
		sPath = sPathOrig;
	}

	const WCHAR chLast = sPath[sPath.length()-1];
	if(chLast != L'/')
	{
		return sPath + L"/";
	}
	return sPath;
}

//------------------------------------------------------
// Get all strings that can match specified domain.
// Result is returned sorted in order from the most common matches to the closest ones.

std::vector<std::wstring> GetAllMatchingDomainKeys(const std::wstring& sDomain)
{
	// NOTE! First-level domains are ignored! (like ".com")
	// parse com -> {}
	// parse domain.com -> {".domain.com", "domain.com"}
	// parse www.domain.com -> {".domain.com", ".www.domain.com", "www.domain.com"}

	std::vector<std::wstring> vectMatchingDomainKeys;
	int nDotsFound = 0;
	size_t pos = sDomain.length()-1;
	while(true)
	{
		pos = sDomain.rfind(L'.', pos);
		if(std::wstring::npos == pos)
		{
			if(nDotsFound > 0)
			{
				vectMatchingDomainKeys.push_back(L"." + sDomain);
				vectMatchingDomainKeys.push_back(sDomain);
			}
			break;
		}
		nDotsFound++;
		if(nDotsFound > 1)
		{
			vectMatchingDomainKeys.push_back(sDomain.substr(pos));
		}
		if(0 == pos)
		{
			break;
		}
		pos--;
	}
	return vectMatchingDomainKeys;
}

//------------------------------------------------------
// Get all strings that can match specified path.
// Result is returned sorted in order from the most common matches to the closest ones.

std::vector<std::wstring> GetAllMatchingPathKeys(const std::wstring& sPath)
{
	// parse / -> {"/", ""}
	// parse /dir1 -> {"/", "/dir1"}
	// parse /dir1/ -> {"/", "/dir1/"}

	std::vector<std::wstring> vectMatchingPathKeys;
	size_t pos = 0;
	while(true)
	{
		pos = sPath.find(L'/', pos);
		if(std::wstring::npos == pos)
		{
			vectMatchingPathKeys.push_back(sPath); // add "/dir/dir2/file"
			break;
		}
		vectMatchingPathKeys.push_back(sPath.substr(0, pos+1));
		if(sPath.length() == pos + 1)
		{
			// '/' was found at last position.
			if(pos == 0)
			{
				vectMatchingPathKeys.push_back(L"");	// function input is L"/"
			}
			break;
		}
		pos++;
	}
	return vectMatchingPathKeys;
}

//------------------------------------------------------

WinHttpCookies::WinHttpCookies(void)
{
	m_nLastFlushUnixTimeGMT = 0;
}

WinHttpCookies::~WinHttpCookies(void)
{
	ClearAllCookies();
}

//------------------------------------------------------

void WinHttpCookies::ClearAllCookies()
{
	SafeCSLock lock(m_protect);
	m_cookies.clear();
}

//------------------------------------------------------

void WinHttpCookies::ClearDomainCookies(const std::wstring& sDomainToRemove) // with subdomains. Example: "google.com"
{
	SafeCSLock lock(m_protect);

	const std::wstring sDomainToRemoveLower = ToLower(sDomainToRemove);	// domain are case-insensitive
	const std::wstring sDottedDomainToRemoveLower = Domain2AllSubDomains(sDomainToRemoveLower);

	HttpCookieContainer::iterator iterDomain;
	for(iterDomain = m_cookies.begin(); iterDomain != m_cookies.end(); ) // NOTE! There is no increment here!
	{
		const std::wstring& sDomain = iterDomain->first;
		if(sDomainToRemoveLower == sDomain || ends(sDomain, sDottedDomainToRemoveLower))
		{
			iterDomain = m_cookies.erase(iterDomain);
			continue;
		}
		iterDomain++;
	}
}

//------------------------------------------------------

void WinHttpCookies::ClearOutdatedCookies(const time_t nCurrentUnixTimeGMT)	// GMT
{
	SafeCSLock lock(m_protect);

	HttpCookieContainer::iterator iterDomain;
	for(iterDomain = m_cookies.begin(); iterDomain != m_cookies.end(); ) // NOTE! There is no increment here!
	{
		HttpCookieDomain& domain = iterDomain->second;

		HttpCookieDomain::iterator iterPath;
		for(iterPath = domain.begin(); iterPath != domain.end(); ) // NOTE! There is no increment here!
		{
			HttpCookiePath& path = iterPath->second;

			HttpCookiePath::iterator iterCookie;
			for(iterCookie = path.begin(); iterCookie != path.end(); ) // NOTE! There is no increment here!
			{
				const HttpCookie& cookie = *iterCookie;
				if(cookie.m_nExpiresGMT < nCurrentUnixTimeGMT)
				{
					iterCookie = path.erase(iterCookie);
					continue;
				}
				iterCookie++;
			}

			if(path.empty())
			{
				iterPath = domain.erase(iterPath);
				continue;
			}
			iterPath++;
		}

		if(domain.empty())
		{
			iterDomain = m_cookies.erase(iterDomain);
			continue;
		}
		iterDomain++;
	}
	m_nLastFlushUnixTimeGMT = nCurrentUnixTimeGMT;
}

//------------------------------------------------------

bool IsValidCookieDomain(const std::wstring& sRequestDomain, const std::wstring& sCookieDomain)
{
	// Domain may set cookie only for it's parent domain (but not ".com") and for its subdomain.
	// Subdomain mask may be used (".domain.com" - first dot is a mask)

	const std::wstring& sDottedRequestDomainLower = ToLower(Domain2AllSubDomains(sRequestDomain));	// domain are case-insensitive
	const std::wstring& sDottedCookieDomainLower = ToLower(Domain2AllSubDomains(sCookieDomain));	// domain are case-insensitive

	if(sDottedCookieDomainLower.find(L'.', 1) == std::wstring::npos)
	{
		return false; // trying to set first-level domain cookie!
	}

	if(ends(sDottedCookieDomainLower, sDottedRequestDomainLower)
		|| ends(sDottedRequestDomainLower, sDottedCookieDomainLower))
	{
		return true;
	}
	return false;
}

//------------------------------------------------------

bool IsValidCookiePath(const std::wstring& sRequestPath, const std::wstring& sCookiePath)
{
	// Path should be a parent path or child path, not an alternate one.
	// '/' at the end of path is used as a child mask.

	const std::wstring& sEndedRequestPath = Path2AllSubFolders(sRequestPath);	// paths are case-sensitive
	const std::wstring& sEndedCookiePath = Path2AllSubFolders(sCookiePath);	// paths are case-sensitive

	if(begins(sEndedRequestPath, sEndedCookiePath)
		|| begins(sEndedCookiePath, sEndedRequestPath))
	{
		return true;
	}
	return false;
}

//------------------------------------------------------

bool WinHttpCookies::CheckSetCookiePolicies(
		const std::wstring& sCookieDomain, const std::wstring& sCookiePath,
		const std::wstring& sRequestDomain, const std::wstring& sRequestPath)
{
	// Note! No SafeCSLock is needed because this is a static class member!

	// TODO: CONTROL: Make Assert on using cookie paths other than '/' or domains without '.' before
 // TODO: COOK
	// TODO: decode path symbols like %20, ignore everything after '#' or '?'.
	// TODO: merge between domains on different ports

	const bool bValidDomain = IsValidCookieDomain(sRequestDomain, sCookieDomain);
	if(!bValidDomain)
	{
		return false;
	}
	const bool bValidPath = IsValidCookiePath(sRequestPath, sCookiePath);
	return bValidPath;
}

//------------------------------------------------------

void WinHttpCookies::SetCookie(const HttpCookie& insertedCookie)
{
	SafeCSLock lock(m_protect);

	// Domain names are case-insensitive (RFC 2616, see above!)
	// Wikipedia, "HTTP cookie": According to section 3.1 of RFC 2965, cookie names are case insensitive.

	const std::wstring& sNormalizedDomainKey = ToLower(insertedCookie.m_sDomain);	// domains are case-insensitive
	const std::wstring& sNormalizedPathKey = insertedCookie.m_sPath;				// URL paths are case-sensitive
	const std::wstring& sNormalizedNameKey = ToLower(insertedCookie.m_sName);		// cookie names are case-insensitive
	HttpCookiePath& path = m_cookies[sNormalizedDomainKey][sNormalizedPathKey];
	HttpCookiePath::iterator iterCookie = path.GetCookie(sNormalizedNameKey);
	if(iterCookie == path.end())
	{
		path.push_back(insertedCookie);
		std::sort(path.begin(), path.end());
	}
	else
	{
		HttpCookie& cookie = *iterCookie;
		const time_t nCreatedGMT = cookie.m_nCreatedGMT;	// store previous creation time
		const SYSTEMTIME stCreated = cookie.m_stCreated;	// store previous creation time
		cookie = insertedCookie;
		cookie.m_nCreatedGMT = nCreatedGMT;					// restore previous creation time
		cookie.m_stCreated = stCreated;						// restore previous creation time
		std::sort(path.begin(), path.end());
	}
}

//------------------------------------------------------

void WinHttpCookies::GetAllCookies(HttpCookieVector& cookiesResult) const
{
	SafeCSLock lock(m_protect);

	cookiesResult.clear();

	HttpCookieContainer::const_iterator iterDomain;
	for(iterDomain = m_cookies.begin(); iterDomain != m_cookies.end(); iterDomain++)
	{
		const HttpCookieDomain& domain = iterDomain->second;

		HttpCookieDomain::const_iterator iterPath;
		for(iterPath = domain.begin(); iterPath != domain.end(); iterPath++)
		{
			const HttpCookiePath& path = iterPath->second;

			HttpCookiePath::const_iterator iterCookie;
			for(iterCookie = path.begin(); iterCookie != path.end(); iterCookie++)
			{
				const HttpCookie& cookie = *iterCookie;
				cookiesResult.push_back(cookie);
			}
		}
	}
}

//------------------------------------------------------

void WinHttpCookies::GetDomainPathCookies(
						  const time_t nCurrentUnixTime, const bool bHttps,
						  const std::wstring& sRequestDomain, const std::wstring& sRequestPath,
						  HttpCookiePathVector& cookiesResult) const
{
	SafeCSLock lock(m_protect);

	cookiesResult.clear();

	HttpCookiePath cookies;

	const std::vector<std::wstring>& vectMatchingDomainKeys = GetAllMatchingDomainKeys(sRequestDomain);
	const std::vector<std::wstring>& vectMatchingPathKeys = GetAllMatchingPathKeys(sRequestPath);

	for(size_t catchingDomain = 0; catchingDomain < vectMatchingDomainKeys.size(); catchingDomain++)
	{
		const std::wstring& sMatchingDomainKey = vectMatchingDomainKeys[catchingDomain];
		HttpCookieContainer::const_iterator iterDomain = m_cookies.find(sMatchingDomainKey);
		if(iterDomain != m_cookies.end())
		{
			const HttpCookieDomain& domain = iterDomain->second;

			for(size_t catchingPath = 0; catchingPath < vectMatchingPathKeys.size(); catchingPath++)
			{
				const std::wstring& sMatchingPathKey = vectMatchingPathKeys[catchingPath];
				HttpCookieDomain::const_iterator iterPath = domain.find(sMatchingPathKey);
				if(iterPath != domain.end())
				{
					const HttpCookiePath& path = iterPath->second;

					HttpCookiePath::const_iterator iterCookie;
					for(iterCookie = path.begin(); iterCookie != path.end(); iterCookie++)
					{
						const HttpCookie& cookie = *iterCookie;
						if(cookie.m_bSecure && !bHttps)
							continue;

						if(cookie.m_nExpiresGMT < nCurrentUnixTime)
							continue;

						HttpCookiePath::iterator iter = cookies.GetCookie(ToLower(cookie.m_sName));
						if(iter == cookies.end())
						{
							cookies.push_back(cookie);
						}
						else
						{
							*iter = cookie;
						}
						//cookies[cookie.m_sName] = cookie; // m_sName is normalized already
						// NOTE! Above statement may replace cookie set by higher ancestor!
					}
				}
			}
		}
	}

	{
		HttpCookiePath::const_iterator iterCookie;
		for(iterCookie = cookies.begin(); iterCookie != cookies.end(); iterCookie++)
		{
			const HttpCookie& cookie = *iterCookie;
			cookiesResult.push_back(cookie);
		}
		std::sort(cookiesResult.begin(), cookiesResult.end());
	}
}

//------------------------------------------------------
//------------------------------------------------------
//------------------------------------------------------

bool CrackSetCookieString(LPCWSTR szCookie, HttpSetCookieInfo& info)
{
	// The Set-Cookie response header uses the following format:
	//
	// Set-Cookie: <name>=<value>[; <name>=<value>]...
	// [; expires=<date>][; domain=<domain_name>]
	// [; path=<some_path>][; secure][; httponly]

	info.m_vectCookies.clear();
	info.m_sExpires.clear();
	info.m_sDomain.clear();
	info.m_sPath.clear();
	info.m_bSecure = false;
	info.m_bHttpOnly = false;

	std::vector<std::wstring> vectTokens;

#if 0
	const int nBufSize = wcslen(szCookie)+1;
	std::array_ptr<WCHAR> ptrBuffer(new WCHAR[nBufSize]);
	WCHAR* pBuffer = ptrBuffer.get();
#if _MSC_VER <= 1200
	wcscpy(pBuffer, szCookie);
#else
	wcscpy_s(pBuffer, nBufSize, szCookie);
#endif

	LPCWSTR seps = L";";
	LPWSTR token = NULL;

#if _MSC_VER <= 1200
	token = wcstok(pBuffer, seps);
	while(token != NULL)
	{
		vectTokens.push_back(token);
		token = wcstok(NULL, seps);
	}
#else
	WCHAR* context = NULL;
	token = wcstok_s(pBuffer, seps, &context);
	while(token != NULL)
	{
		vectTokens.push_back(token);
		token = wcstok_s(NULL, seps, &context);
	}
#endif
#else
	BreakStringToTokens(vectTokens, szCookie, L";");
#endif

	for(size_t iToken = 0; iToken < vectTokens.size(); iToken++)
	{
		const std::wstring& s = vectTokens[iToken];
		std::wstring sName, sValue;
		const size_t posFirst = s.find_first_not_of(L' ');
		const size_t posEqual = s.find(L'=');
		if(posFirst == std::wstring::npos)
		{
			return false;
		}
		if(posEqual == std::wstring::npos)
		{
			sName = s.substr(posFirst);
		}
		else
		{
			sName = s.substr(posFirst, posEqual - posFirst);
			sValue = s.substr(posEqual + 1);
		}

		if(AreStringsEqualNoCase(L"expires", sName.c_str()))
		{
			info.m_sExpires = sValue;
		}
		else if(AreStringsEqualNoCase(L"domain", sName.c_str()))
		{
			info.m_sDomain = sValue;
		}
		else if(AreStringsEqualNoCase(L"path", sName.c_str()))
		{
			info.m_sPath = sValue;
		}
		else if(AreStringsEqualNoCase(L"secure", sName.c_str()))
		{
			info.m_bSecure = true;
		}
		else if(AreStringsEqualNoCase(L"httponly", sName.c_str()))
		{
			info.m_bHttpOnly = true;
		}
		else
		{
			HttpSetCookieInfo::NameValue cookie;
			cookie.m_sName = sName;
			cookie.m_sValue = sValue;
			info.m_vectCookies.push_back(cookie);
		}
	}
	return true;
}

//------------------------------------------------------

std::string MyWinHttpDecodeHttpEncodedChars(const std::string& sText) // Decode %XX patterns, output is UTF8
{
	std::string res;
	size_t pos = 0;
	const size_t len = sText.length();
	while(true)
	{
		const size_t pos2 = sText.find('%', pos);
		if(pos2 == std::string::npos || pos2 + 2 >= len)
		{
			res += sText.substr(pos);
			break;
		}
		if(pos2 > pos)
		{
			res += sText.substr(pos, pos2-pos);
		}
		const char hex[3] = {sText[pos2+1], sText[pos2+2], 0};
		const unsigned long nChar = strtoul(hex, NULL, 16);
		res += (char)nChar;
		pos = pos2 + 3;
	}
	return res;
}

//------------------------------------------------------

std::string MyWinHttpEncodeHttpEncodedChars(const std::string& sText) // Encode %XX patterns, input is UTF8
{
	std::string s;
	for(size_t i = 0; i < sText.length(); i++)
	{
		char ch = sText[i];
		if(
			('A' <= ch && ch <= 'Z') ||
			('a' <= ch && ch <= 'z') ||
			('0' <= ch && ch <= '9') ||
			ch == '-' || ch == '_' || ch == '.' || ch == '*'
			)
		{
			s += ch;
		}
		else
		{
// 			if(ch == ' ')
// 			{
// 				s += '+'; // NOTE! Space (' ') is encoded as '+' by RFC 2396!!!
// 			}
// 			else
			{
				s += StdString::Format("%%%02X", (UCHAR)ch);
			}
		}
	}
	return s;
}

//------------------------------------------------------

void PreProcessCookies(const HttpSetCookieInfo& info,
					   const SYSTEMTIME& stNow,
					   HttpCookieVector& cookies)
{
	cookies.clear();

//	const time_t tParsedTime = TM2TIMET(ParseHttpDateTime(UnicodeToUtf8(info.m_sExpires)));		// GMT
	SYSTEMTIME st;
	MyWinHttpTimeToSystemTime(info.m_sExpires, &st);
	const time_t tParsedTime = TM2TIMET(ST2TM(st));			// GMT

	const time_t nUnixTimeExpire = tParsedTime == 0 ? HttpCookie::EXPIRE_SESSION : tParsedTime;	// GMT
	const SYSTEMTIME stExpires = GMT2Locale(TM2ST(TIMET2TM(nUnixTimeExpire)));	// local time

	const size_t n = info.m_vectCookies.size();
	cookies.resize(n);
	for(size_t i = 0; i < n; i++)
	{
		const HttpSetCookieInfo::NameValue& pair = info.m_vectCookies[i];
		HttpCookie& cookie = cookies[i];

		cookie.m_sName = pair.m_sName;
		cookie.m_sRawHttpValue = pair.m_sValue;
//		cookie.m_sDecodedValue = Utf8ToUnicode(DecodeHttpEncodedChars(UnicodeToUtf8(cookie.m_sRawHttpValue)));
		cookie.m_sDecodedValue = Utf8ToUnicode(MyWinHttpDecodeHttpEncodedChars(UnicodeToUtf8(cookie.m_sRawHttpValue)));

		cookie.m_sExpires = info.m_sExpires;
		cookie.m_sDomain = info.m_sDomain;
		cookie.m_sPath = info.m_sPath;
		cookie.m_bSecure = info.m_bSecure;
		cookie.m_bHttpOnly = info.m_bHttpOnly;

		cookie.m_nCreatedGMT = TM2TIMET(ST2TM(Locale2GMT(stNow)));
		cookie.m_nUpdatedGMT = cookie.m_nCreatedGMT;
		cookie.m_nExpiresGMT = nUnixTimeExpire;

		cookie.m_stCreated = stNow;
		cookie.m_stUpdated = stNow;

		cookie.m_stExpires = stExpires;
	}
}

//------------------------------------------------------

HttpCookie::HttpCookie(const std::wstring& sName, const std::wstring& sValue, const bool bRawHttpValue,
					   const std::wstring& sDomain, const SYSTEMTIME& stNow) // local time
{
	m_bSecure = false;
	m_bHttpOnly = false;

	m_nCreatedGMT = TM2TIMET(ST2TM(Locale2GMT(stNow)));
	m_nUpdatedGMT = m_nCreatedGMT;
	m_nExpiresGMT = HttpCookie::EXPIRE_SESSION;

	m_stCreated = stNow;
	m_stUpdated = stNow;
	EmptySystemTime(m_stExpires);

	m_sName = sName;
	m_sPath = L"/";
	m_sDomain = sDomain;
	if(bRawHttpValue)
	{
		m_sRawHttpValue = sValue;
		m_sDecodedValue = Utf8ToUnicode(MyWinHttpDecodeHttpEncodedChars(UnicodeToUtf8(sValue)));
	}
	else
	{
		m_sRawHttpValue = Utf8ToUnicode(MyWinHttpEncodeHttpEncodedChars(UnicodeToUtf8(sValue)));
		m_sDecodedValue = sValue;
	}
}

//------------------------------------------------------

void HttpCookie::EmptySystemTime(SYSTEMTIME& st)
{
	st.wYear = 0;
	st.wMonth = 0;
	st.wDayOfWeek = 0;
	st.wDay = 0;
	st.wHour = 0;
	st.wMinute = 0;
	st.wSecond = 0;
	st.wMilliseconds = 0;
}

//------------------------------------------------------

std::wstring HttpCookie::Dump() const	// for Debug purposes
{
	// Set-Cookie: <name>=<value>[; <name>=<value>]...
	// [; expires=<date>][; domain=<domain_name>]
	// [; path=<some_path>][; secure][; httponly]
	std::wstring res;
	res += L"Set-Cookie: " + m_sName + L"=" + m_sRawHttpValue;
	if(!m_sExpires.empty())
		res += L"; expires=" + m_sExpires;
	if(!m_sDomain.empty())
		res += L"; domain=" + m_sDomain;
	if(!m_sPath.empty())
		res += L"; path=" + m_sPath;
	if(m_bSecure)
		res += L"; secure";
	if(m_bHttpOnly)
		res += L"; httponly";
	res += StdString::FormatW(L" | expiresUnix=%I64d; created=%hs; updated=%hs; expiresInt=%hs;",
		INT64(m_nExpiresGMT), GetStringDateTime(m_stCreated).c_str(),
		GetStringDateTime(m_stUpdated).c_str(), GetStringDateTime(m_stExpires).c_str()
		);
	return res;
}

//------------------------------------------------------

std::wstring DumpHttpCookies(const HttpCookieVector& cookies)	// for Debug purposes
{
	// Set-Cookie: <name>=<value>[; <name>=<value>]...
	// [; expires=<date>][; domain=<domain_name>]
	// [; path=<some_path>][; secure][; httponly]
	std::wstring res;
	const size_t n = cookies.size();
	for(size_t i = 0; i < n; i++)
	{
		const HttpCookie& cookie = cookies[i];
		res += cookie.Dump();
		res += L"\n";
	}
	return res;
}

//------------------------------------------------------
//------------------------------------------------------
//------------------------------------------------------
//------------------------------------------------------
