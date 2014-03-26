// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2013
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "TextHelpers.h"

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <wchar.h>
#include <string.h>
#include <mbstring.h>
#include <locale.h>
#include <errno.h>
#include <locale.h>

#include <atlbase.h>
#include <atlconv.h>

//#include <sstream>

#include <vector>

#include "Tracing.h"
#include "array_ptr.h"

#include <winhttp.h>	// for WINHTTP_ERROR_BASE, WINHTTP_ERROR_LAST

#include <Shlwapi.h>	// StrStrI, StrStrIW, StrCmpNI, StrChrIA, StrChrIW
#pragma comment(lib, "shlwapi.lib")

#ifdef UNITTEST_DEPENDENCY
#include "UnitTest\UnitTest.h"
IMPLEMENT_MODULE(TextHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

#ifdef _DEBUG
#pragma comment(lib, "comsuppwd.lib")
#else
#pragma comment(lib, "comsuppw.lib")
#endif

//////////////////////////////////////////////////////////////////////

#define START_DYN_BUFFER_SIZE	512
#define MAX_DYN_BUFFER_SIZE		(128*1024*1024)
#define RESIZE_DYN_BUFFER_MULT	2
#define RESIZE_DYN_BUFFER_DIV	1

#define MY_ERRNO_NO_CHANGE		(-100)
#define MY_ERRNO_EXCEPTION		(-101)

//////////////////////////////////////////////////////////////////////

bool g_bQuietStringFormatting = false;

class QuietStringFormatting
{
	bool bOldValue;
public:
	inline QuietStringFormatting()
	{
		bOldValue = g_bQuietStringFormatting;
		g_bQuietStringFormatting = true;
	}
	inline ~QuietStringFormatting()
	{
		g_bQuietStringFormatting = bOldValue;
	}
};

//////////////////////////////////////////////////////////////////////

int ProcessFormatException(const void* pszFormat, const bool bUnicode)
{
	if(!g_bQuietStringFormatting)
	{
		LPCSTR pszType = bUnicode ? "Unicode" : "Ansi";

		if(pszFormat == NULL)
		{
			MYTRACE("ERROR! String formatting failed with a NULL %s format string!\n", pszType);
		}
		else
		{
			if(bUnicode)
				MYTRACE("ERROR! String formatting failed with %s format string \"%ls\"!\n", pszType, pszFormat);
			else
				MYTRACE("ERROR! String formatting failed with %s format string \"%hs\"!\n", pszType, pszFormat);
		}
		MYASSERT(FALSE && "String formatting failed! (CONTINUE will produce empty string)");
	}

	errno = MY_ERRNO_EXCEPTION; // my internal special value
	return -1;
}

//////////////////////////////////////////////////////////////////////

int ProcessFormatResults(const void* pszFormat, const int nErrno, const int nBufferSize, const bool bUnicode)
{
	if(nBufferSize >= MAX_DYN_BUFFER_SIZE)
	{
		if(!g_bQuietStringFormatting)
		{
			LPCSTR pszType = bUnicode ? "Unicode" : "Ansi";
			MYTRACE("ERROR! %s formatting allocated too many memory! (%d bytes)\n", pszType, nBufferSize);
		}
		return 0; // break loop, return empty string
	}

	if(nErrno == ERANGE || nErrno == MY_ERRNO_NO_CHANGE)
	{
		// need greater buffer
		return -1; // continue loop, try bigger buffer
	}

	if(nErrno == MY_ERRNO_EXCEPTION)
	{
		// formatting failed with exception (see ProcessFormatException())
		return 0; // break loop, return empty string
	}

	if(nErrno == EINVAL)
	{
		// formatting error, but execution was allowed to continue
		ProcessFormatException(pszFormat, bUnicode);
		return 0; // break loop, return empty string
	}

	if(!g_bQuietStringFormatting)
	{
		LPCSTR pszType = bUnicode ? "Unicode" : "Ansi";
		MYTRACE("ERROR: %s formatting got errno = %d\n", pszType, nErrno);
		if(nErrno == EILSEQ)
		{
			const std::string sLocale = setlocale(LC_ALL, NULL); // read current locale
			if(sLocale == "C")
			{
				MYTRACE("NOTE! You should call setlocale(LC_ALL, \"English\") to set correct locale!\n");
			}
			if(pszFormat != NULL)
			{
				if(bUnicode)
					MYTRACE("ERROR! Unsupported unicode sequence found in arguments for %s format string \"%ls\"!\n", pszType, pszFormat);
				else
					MYTRACE("ERROR! Unsupported unicode sequence found in arguments for %s format string \"%hs\"!\n", pszType, pszFormat);
			}
		}
	}

	return 0; // break loop, return empty string
}

//////////////////////////////////////////////////////////////////////

std::string StdString::Format(LPCSTR pszFormat, ...)
{
	va_list vl;
	va_start(vl, pszFormat);
	return StdString::FormatV(pszFormat, vl);

// 	va_list vl;
// 	va_start(vl, pszFormat);
//
// 	int nBufferSize = START_DYN_BUFFER_SIZE;
// 	int nRetVal = -1;
// 	int nErrno = MY_ERRNO_NO_CHANGE;
//
// 	std::vector<CHAR> arrTemp;
// 	CHAR* pszTemp = NULL;
//
// 	do
//	{
// 		nBufferSize = nBufferSize * RESIZE_DYN_BUFFER_MULT / RESIZE_DYN_BUFFER_DIV;
// 		arrTemp.resize( nBufferSize );
// 		pszTemp = &arrTemp.front();
//
// 		errno = MY_ERRNO_NO_CHANGE;
// 		try
// 		{
// 			nRetVal = _vsnprintf_s(pszTemp, nBufferSize, nBufferSize - 1, pszFormat, vl);
// 		}
// 		catch(...)
// 		{
// 			nRetVal = ProcessFormatException(pszFormat, false);
// 		}
// 		nErrno = errno;
// 		if( nRetVal == nBufferSize - 1 )
// 		{
// 			// write terminating character if the buffer is the same length as result
// 			pszTemp[nBufferSize - 1] = ('\0');
// 		}
// 		if(nRetVal < 0 || nBufferSize >= MAX_DYN_BUFFER_SIZE)
// 		{
// 			nRetVal = ProcessFormatResults(pszFormat, nErrno, nBufferSize, false);
// 		}
// 	} while (nRetVal == -1);
//
// 	va_end(vl);
// 	if(nRetVal <= 0)
// 		return "";
// 	return pszTemp;
}

//////////////////////////////////////////////////////////////////////////

std::wstring StdString::FormatW(LPCWSTR pszFormat, ...)
{
	va_list vl;
	va_start(vl, pszFormat);
	return StdString::FormatVW(pszFormat, vl);

// 	va_list vl;
// 	va_start(vl, pszFormat);
//
// 	int nBufferSize = START_DYN_BUFFER_SIZE;
// 	int nRetVal = -1;
// 	int nErrno = MY_ERRNO_NO_CHANGE;
//
// 	std::vector<WCHAR> arrTemp;
// 	WCHAR* pszTemp = NULL;
//
// 	do
//	{
// 		nBufferSize = nBufferSize * RESIZE_DYN_BUFFER_MULT / RESIZE_DYN_BUFFER_DIV;
// 		arrTemp.resize( nBufferSize );
// 		pszTemp = &arrTemp.front();
//
// 		errno = MY_ERRNO_NO_CHANGE;
// 		try
// 		{
// 			nRetVal = _vsnwprintf_s(pszTemp, nBufferSize, nBufferSize - 1, pszFormat, vl);
// 		}
// 		catch(...)
// 		{
// 			nRetVal = ProcessFormatException(pszFormat, true);
// 		}
// 		nErrno = errno;
// 		if( nRetVal == nBufferSize - 1 )
// 		{
// 			// write terminating character if the buffer is the same length as result
// 			pszTemp[nBufferSize - 1] = L'\0';
// 		}
// 		if(nRetVal < 0 || nBufferSize >= MAX_DYN_BUFFER_SIZE)
// 		{
// 			nRetVal = ProcessFormatResults(pszFormat, nErrno, nBufferSize, true);
// 		}
// 	} while (nRetVal == -1);
//
// 	va_end(vl);
// 	if(nRetVal <= 0)
// 		return L"";
// 	return pszTemp;
}

//////////////////////////////////////////////////////////////////////////

std::string StdString::FormatV(LPCSTR pszFormat, va_list argList)
{
	int nBufferSize = START_DYN_BUFFER_SIZE;
	int nRetVal = -1;
	int nErrno = MY_ERRNO_NO_CHANGE;

	std::vector<CHAR> arrTemp;
	CHAR* pszTemp = NULL;

	do
	{
		nBufferSize = nBufferSize * RESIZE_DYN_BUFFER_MULT / RESIZE_DYN_BUFFER_DIV;
		arrTemp.resize( nBufferSize );
		pszTemp = &arrTemp.front();

		errno = MY_ERRNO_NO_CHANGE;
		try
		{
			nRetVal = _vsnprintf_s(pszTemp, nBufferSize, nBufferSize - 1, pszFormat, argList);
		}
		catch(...)
		{
			nRetVal = ProcessFormatException(pszFormat, false);
		}
		nErrno = errno;
		if( nRetVal == nBufferSize - 1 )
		{
			// write terminating character if the buffer is the same length as result
			//printf( "Writing terminating NULL!!!\n" );
			pszTemp[nBufferSize - 1] = '\0';
		}
		if(nRetVal < 0 || nBufferSize >= MAX_DYN_BUFFER_SIZE)
		{
			nRetVal = ProcessFormatResults(pszFormat, nErrno, nBufferSize, false);
		}
	} while (nRetVal == -1);

	if(nRetVal <= 0)
		return std::string();
	return pszTemp;
}

//////////////////////////////////////////////////////////////////////////

std::wstring StdString::FormatVW(LPCWSTR pszFormat, va_list argList)
{
	int nBufferSize = START_DYN_BUFFER_SIZE;
	int nRetVal = -1;
	int nErrno = MY_ERRNO_NO_CHANGE;

	std::vector<WCHAR> arrTemp;
	WCHAR* pszTemp = NULL;

	do
	{
		nBufferSize = nBufferSize * RESIZE_DYN_BUFFER_MULT / RESIZE_DYN_BUFFER_DIV;
		arrTemp.resize( nBufferSize );
		pszTemp = &arrTemp.front();

		errno = MY_ERRNO_NO_CHANGE;
		try
		{
			nRetVal = _vsnwprintf_s(pszTemp, nBufferSize, nBufferSize - 1, pszFormat, argList);
		}
		catch(...)
		{
			nRetVal = ProcessFormatException(pszFormat, true);
		}
		nErrno = errno;
		if( nRetVal == nBufferSize - 1 )
		{
			// write terminating character if the buffer is the same length as result
			//printf( "Writing terminating NULL!!!\n" );
			pszTemp[nBufferSize - 1] = L'\0';
		}
		if(nRetVal < 0 || nBufferSize >= MAX_DYN_BUFFER_SIZE)
		{
			nRetVal = ProcessFormatResults(pszFormat, nErrno, nBufferSize, true);
		}
	} while (nRetVal == -1);

	if(nRetVal <= 0)
		return std::wstring();
	return pszTemp;
}

//////////////////////////////////////////////////////////////////////////
// NOTE! Following function was got from MSDN article "C/C++ Code Example: Reading Error Codes":

HRESULT GetKnownLibraryErrorText(
					LPCTSTR pszLibrary,
                    DWORD dwErrorCode,
                    TCHAR **ppErrorText,
                    DWORD *pdwSize
                    )
{
	HMODULE hMod = NULL;
	TCHAR * pMsgBuf = NULL;
	DWORD dwSize = 0;
	hMod = LoadLibrary(pszLibrary);

	if (hMod)
	{
		// Use the FormatMessage API to translate the error code
		{
			const DWORD dwEnglishLanguageId = MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US);

			dwSize = FormatMessage(FORMAT_MESSAGE_FROM_HMODULE |
				FORMAT_MESSAGE_ALLOCATE_BUFFER |
				FORMAT_MESSAGE_IGNORE_INSERTS,
				hMod, dwErrorCode, dwEnglishLanguageId, (LPTSTR)&pMsgBuf, 0, NULL);

			if(dwSize == 0 && ERROR_RESOURCE_LANG_NOT_FOUND == GetLastError())
			{
				dwSize = FormatMessage(FORMAT_MESSAGE_FROM_HMODULE |
					FORMAT_MESSAGE_ALLOCATE_BUFFER |
					FORMAT_MESSAGE_IGNORE_INSERTS,
					hMod, dwErrorCode, 0, (LPTSTR)&pMsgBuf, 0, NULL);
			}
		}

		FreeLibrary(hMod);

		// Return the description and size to the caller in the OUT parameters.
		if (dwSize > 0)
		{
			*pdwSize = dwSize;
			*ppErrorText = pMsgBuf;
			return S_OK;
		}
	}

	// Return the error code.
	return HRESULT_FROM_WIN32(GetLastError());
}

//////////////////////////////////////////////////////////////////////////

#define MQDLL_W_ERROR_TEXT		TEXT("MQutil.dll")
#define WINHTTP_W_ERROR_TEXT	TEXT("winhttp.dll")

HRESULT GetErrorText(
					DWORD dwErrorCode,
					TCHAR **ppErrorText,
					DWORD *pdwSize
					)
{
	HMODULE hMod = NULL;
	TCHAR * pMsgBuf = NULL;
	DWORD dwSize = 0;

	// Validate the input parameters
	if (ppErrorText == NULL || pdwSize == NULL)
	{
		return HRESULT_FROM_WIN32(ERROR_INVALID_PARAMETER);
	}

	// Initialize the two OUT parameters
	*ppErrorText = NULL;
	*pdwSize = 0;

	if(WINHTTP_ERROR_BASE <= dwErrorCode && dwErrorCode <= WINHTTP_ERROR_LAST)
	{
		return GetKnownLibraryErrorText(WINHTTP_W_ERROR_TEXT, dwErrorCode, ppErrorText, pdwSize);
	}

	DWORD dwFacility = HRESULT_FACILITY(dwErrorCode);
	switch(dwFacility)
	{
	case FACILITY_MSMQ:
		return GetKnownLibraryErrorText(MQDLL_W_ERROR_TEXT, dwErrorCode, ppErrorText, pdwSize);
// 		// Load MQDLL_W_ERROR_TEXT DLL, i.e., MQutil.dll
// 		hMod = LoadLibrary(MQDLL_W_ERROR_TEXT);
//
// 		if (hMod)
// 		{
// 			// Use the FormatMessage API to translate the error code
// 			dwSize = FormatMessage(FORMAT_MESSAGE_FROM_HMODULE |
// 				FORMAT_MESSAGE_ALLOCATE_BUFFER |
// 				FORMAT_MESSAGE_IGNORE_INSERTS,
// 				hMod, dwErrorCode, 0, (LPTSTR)&pMsgBuf, 0, NULL);
//
// 			// Unload MQDLL_W_ERROR_TEXT DLL, i.e., MQutil.dll.
// 			FreeLibrary(hMod);
//
// 			// Return the description and size to the caller in the OUT parameters.
// 			if (dwSize)
// 			{
// 				*pdwSize = dwSize;
// 				*ppErrorText = (TCHAR*)pMsgBuf;
// 				return S_OK;
// 			}
// 		}
//
// 		// Return the error code.
// 		return GetLastError();

	case FACILITY_WIN32:
	case FACILITY_NULL:	// raw WinAPI errors (from GetLastError() function)
	default:	// for FACILITY_SECURITY, FACILITY_RPC, ...

		// Retrieve the Win32 error message.
		{
			const DWORD dwEnglishLanguageId = MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US);

			dwSize = FormatMessage(
				FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_IGNORE_INSERTS,
				NULL, dwErrorCode, dwEnglishLanguageId, (LPTSTR) &pMsgBuf, 0, NULL);

			if(dwSize == 0 && ERROR_RESOURCE_LANG_NOT_FOUND == GetLastError())
			{
				dwSize = FormatMessage(
					FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_IGNORE_INSERTS,
					NULL, dwErrorCode, 0, (LPTSTR) &pMsgBuf, 0, NULL);
			}
		}

		// Return the description and size to the caller in the OUT parameters.
		if (dwSize > 0)
		{
			*pdwSize = dwSize;
			*ppErrorText = pMsgBuf;
			return S_OK;
		}

		// Return the error code.
		return HRESULT_FROM_WIN32(GetLastError());
	};

	return HRESULT_FROM_WIN32(ERROR_INVALID_PARAMETER);
}

//////////////////////////////////////////////////////////////////////////

tstring StdString::FormatApiError(LONG errorCode) // Formatting trims \n at the end of string!
{
	tstring res;
	TCHAR* p = NULL;
	DWORD dwChars = 0;
	const int nErrno = errno;
	LONG le = GetLastError(); // save error
	HRESULT hr = GetErrorText(errorCode, &p, &dwChars);
	if(S_OK == hr)
	{
		res = p;
		LocalFree(p);
		if(!res.empty())
		{
			if(res[res.length()-1] == _T('\n'))
			{
				res.erase(res.end()-1);
			}
		}
		if(!res.empty())
		{
			if(res[res.length()-1] == _T('\r'))
			{
				res.erase(res.end()-1);
			}
		}
	}
	else
	{
		res = _T("Unknown_API_Error(") + itot(errorCode) + _T(")");
	}
	SetLastError(le); // restore error
	errno = nErrno;
	return res;
}

//////////////////////////////////////////////////////////////////////////

std::string StdString::FormatApiErrorA(LONG errorCode) // Formatting trims \n at the end of string!
{
#ifdef _UNICODE
	const int nErrno = errno;
	LONG le = GetLastError(); // save error
	USES_CONVERSION;
	const std::string s = CW2A(StdString::FormatApiError(errorCode).c_str());
	SetLastError(le); // restore error
	errno = nErrno;
	return s;
#else
	return StdString::FormatApiError(errorCode);
#endif
}

//////////////////////////////////////////////////////////////////////////

std::wstring StdString::FormatApiErrorW(LONG errorCode) // Formatting trims \n at the end of string!
{
#ifdef _UNICODE
	return StdString::FormatApiError(errorCode);
#else
	const int nErrno = errno;
	LONG le = GetLastError(); // save error
	USES_CONVERSION;
	const std::wstring s = CA2W(StdString::FormatApiError(errorCode).c_str());
	SetLastError(le); // restore error
	errno = nErrno;
	return s;
#endif
}

//////////////////////////////////////////////////////////////////////////

tstring itot(int x) // converts int to string
{
	TCHAR buf[40] = _T("");
	_itot_s(x, buf, 40, 10);
	return buf;
}

//////////////////////////////////////////////////////////////////////////

tstring ltot(long x) // converts long to string
{
	TCHAR buf[40] = _T("");
	_ltot_s(x, buf, 40, 10);
	return buf;
}

//////////////////////////////////////////////////////////////////////////

tstring ultot(unsigned long x) // converts unsigned long to string
{
	TCHAR buf[40] = _T("");
	_ultot_s(x, buf, 40, 10);
	return buf;
}

//////////////////////////////////////////////////////////////////////////

unsigned long mystoul(const std::string& s)
{
	return strtoul(s.c_str(), NULL, 10);
}

unsigned long mystoul(const std::wstring& s)
{
	return wcstoul(s.c_str(), NULL, 10);
}

//////////////////////////////////////////////////////////////////////////

std::string ToLower(const std::string& s)
{
	return UnicodeToUtf8(ToLower(Utf8ToUnicode(s)));
// 	const int nBufSize = s.length()+1;
// 	std::array_ptr<char> p(new char[nBufSize]);
// 	strncpy_s(p.get(), nBufSize, s.c_str(), nBufSize-1);
// 	_strlwr_s(p.get(), nBufSize);
// 	return p.get();
}

//////////////////////////////////////////////////////////////////////////

std::wstring ToLower(const std::wstring& s)
{
	const int nBufSize = s.length()+1;
	std::array_ptr<wchar_t> p(new wchar_t[nBufSize]);
	wcsncpy_s(p.get(), nBufSize, s.c_str(), nBufSize-1);
//	_wcslwr_s(p.get(), nBufSize);
	CharLowerBuffW(p.get(), nBufSize);
	return p.get();
}

//////////////////////////////////////////////////////////////////////////

std::string ToUpper(const std::string& s)
{
	return UnicodeToUtf8(ToUpper(Utf8ToUnicode(s)));
// 	const int nBufSize = s.length()+1;
// 	std::array_ptr<char> p(new char[nBufSize]);
// 	strncpy_s(p.get(), nBufSize, s.c_str(), nBufSize-1);
// 	_strupr_s(p.get(), nBufSize);
// 	return p.get();
}

//////////////////////////////////////////////////////////////////////////

std::wstring ToUpper(const std::wstring& s)
{
	const int nBufSize = s.length()+1;
	std::array_ptr<wchar_t> p(new wchar_t[nBufSize]);
	wcsncpy_s(p.get(), nBufSize, s.c_str(), nBufSize-1);
	//_wcsupr_s(p.get(), nBufSize);
	CharUpperBuffW(p.get(), nBufSize);
	return p.get();
}

//////////////////////////////////////////////////////////////////////////
#if 0 // disabled because it expects ANSI codepage (non UTF8)
char ToLower(const char ch)
{
	return (char)CharLowerA((LPSTR)ch);
}
#endif
//////////////////////////////////////////////////////////////////////////

wchar_t ToLower(const wchar_t ch)
{
	return (wchar_t)CharLowerW((LPWSTR)ch);
}

//////////////////////////////////////////////////////////////////////////
#if 0 // disabled because it expects ANSI codepage (non UTF8)
char ToUpper(const char ch)
{
	return (char)CharUpperA((LPSTR)ch);
}
#endif
//////////////////////////////////////////////////////////////////////////

wchar_t ToUpper(const wchar_t ch)
{
	return (wchar_t)CharUpperW((LPWSTR)ch);
}

//------------------------------------------------------
#if 0 // disabled because it expects ANSI codepage (non UTF8)
size_t StrPosI(const std::string& sText, const std::string& sSubstring, const size_t offset)	// ANSI, returns -1 if not found
{
	if(sText.length() < offset)
		return -1;
	const LPCSTR p1 = sText.c_str();
	if(sSubstring.empty())
		return offset;
	const LPCSTR p2 = StrStrIA(p1 + offset, sSubstring.c_str());
	return p2 == NULL ? -1 : p2 - p1;
}
#endif
//------------------------------------------------------

size_t StrPosI(const std::wstring& sText, const std::wstring& sSubstring, const size_t offset)	// Unicode, returns -1 if not found
{
	if(sText.length() < offset)
		return -1;
	const LPCWSTR p1 = sText.c_str();
	if(sSubstring.empty())
		return offset;
	const LPCWSTR p2 = StrStrIW(p1 + offset, sSubstring.c_str());
	return p2 == NULL ? -1 : p2 - p1;
}

//------------------------------------------------------
//#if 0 // disabled because it expects ANSI codepage (non UTF8)
size_t StrPosI(const std::string& sText, const char ch, const size_t offset)	// ANSI, returns -1 if not found
{
	if(sText.length() < offset)
		return -1;
	const LPCSTR p1 = sText.c_str();
	const LPCSTR p2 = StrChrIA(p1 + offset, ch);
	return p2 == NULL ? -1 : p2 - p1;
}
//#endif
//------------------------------------------------------

size_t StrPosI(const std::wstring& sText, const wchar_t ch, const size_t offset)	// Unicode, returns -1 if not found
{
	if(sText.length() < offset)
		return -1;
	const LPCWSTR p1 = sText.c_str();
	const LPCWSTR p2 = StrChrIW(p1 + offset, ch);
	return p2 == NULL ? -1 : p2 - p1;
}

//------------------------------------------------------

int MyStrCmpNI(LPCWSTR p1, LPCWSTR p2, const size_t nMaxChars)	// Unicode
{
	return StrCmpNI(p1, p2, nMaxChars);
}

//------------------------------------------------------

void BreakStringToTokens(OUT std::vector<tstring>& vectTokens, const tstring& sText, const tstring& sSeparators)
{
	vectTokens.clear();

	const int nBufSize = sText.length() + 1;
	std::array_ptr<TCHAR> ptrBuffer(new TCHAR[nBufSize]);
	TCHAR* pBuffer = ptrBuffer.get();
#if _MSC_VER <= 1200
	_tcscpy(pBuffer, sText.c_str());
#else
	_tcscpy_s(pBuffer, nBufSize, sText.c_str());
#endif

	LPCTSTR seps = sSeparators.c_str();
	LPTSTR token = NULL;

#if _MSC_VER <= 1200
	token = _tcstok(pBuffer, seps);
	while(token != NULL)
	{
		vectTokens.push_back(token);
		token = _tcstok(NULL, seps);
	}
#else
	TCHAR* context = NULL;
	token = _tcstok_s(pBuffer, seps, &context);
	while(token != NULL)
	{
		vectTokens.push_back(token);
		token = _tcstok_s(NULL, seps, &context);
	}
#endif
}

//////////////////////////////////////////////////////////////////////////

void BreakStringToLines(OUT std::vector<std::string>& vectLines, const std::string& sText)
{
	// Note! This function is a bit different from BreakToTokens() because it can return empty strings!
	// Divide text into separate lines:
	vectLines.clear();

	std::string::size_type pos = 0;
	const std::string::const_iterator begin = sText.begin();
	const tstring::size_type len = sText.length();

	while(len > pos)
	{
		const std::string::size_type pos2 = sText.find_first_of(("\r\n"), pos);
		if(std::string::npos == pos2)
		{
			const std::string s(begin + pos, sText.end());
			vectLines.push_back(s);
			break;
		}

		const std::string s(begin + pos, begin + pos2);
		vectLines.push_back(s);

		pos = pos2;

		pos++;
		if(pos2 < len - 1)
		{
			const int ch = sText[pos2];
			const int chNext = sText[pos2+1];
			if(ch == ('\r') && chNext == ('\n'))
				pos++;
			else if(ch == ('\n') && chNext == ('\r'))
				pos++;
		}
	}
}

//////////////////////////////////////////////////////////////////////////

void BreakStringToLines(OUT std::vector<std::wstring>& vectLines, const std::wstring& sText)
{
	// Note! This function is a bit different from BreakToTokens() because it can return empty strings!
	// Divide text into separate lines:
	vectLines.clear();

	std::wstring::size_type pos = 0;
	const std::wstring::const_iterator begin = sText.begin();
	const std::wstring::size_type len = sText.length();

	while(len > pos)
	{
		const std::wstring::size_type pos2 = sText.find_first_of((L"\r\n"), pos);
		if(std::wstring::npos == pos2)
		{
			const std::wstring s(begin + pos, sText.end());
			vectLines.push_back(s);
			break;
		}

		const std::wstring s(begin + pos, begin + pos2);
		vectLines.push_back(s);

		pos = pos2;

		pos++;
		if(pos2 < len - 1)
		{
			const int ch = sText[pos2];
			const int chNext = sText[pos2+1];
			if(ch == (L'\r') && chNext == (L'\n'))
				pos++;
			else if(ch == (L'\n') && chNext == (L'\r'))
				pos++;
		}
	}
}

//------------------------------------------------------

bool BreakStringToKeyValue(const std::string& sText, OUT std::string& sKey, OUT std::string& sValue,
	const std::string& sEqualStr, const std::string& sTrimChars)
{
	const size_t pos = sText.find(sEqualStr);
	if(std::string::npos == pos)
	{
		sKey.clear();
		sValue.clear();
		return false;
	}
	else
	{
		sKey = sText.substr(0, pos);
		sValue = sText.substr(pos + sEqualStr.length());
	}

	if(!sTrimChars.empty())
	{
		sKey = trim(sKey, sTrimChars);
		sValue = trim(sValue, sTrimChars);
	}
	return true;
}

//------------------------------------------------------

bool BreakStringToKeyValue(const std::wstring& sText, OUT std::wstring& sKey, OUT std::wstring& sValue,
	const std::wstring& sEqualStr, const std::wstring& sTrimChars)
{
	const size_t pos = sText.find(sEqualStr);
	if(std::wstring::npos == pos)
	{
		sKey.clear();
		sValue.clear();
		return false;
	}
	else
	{
		sKey = sText.substr(0, pos);
		sValue = sText.substr(pos + sEqualStr.length());
	}

	if(!sTrimChars.empty())
	{
		sKey = trim(sKey, sTrimChars);
		sValue = trim(sValue, sTrimChars);
	}
	return true;
}

//------------------------------------------------------

std::wstring MultibyteToUnicode(const std::string& sSource, const UINT CodePage)
{
	const int nMemSize = MultiByteToWideChar(CodePage, 0, sSource.data(), sSource.size(), NULL, 0);
	if(0 == nMemSize)
	{
		return std::wstring();
	}
	std::array_ptr<WCHAR> p(new WCHAR[nMemSize]);
	if(p.get() == NULL)
	{
		return std::wstring();
	}
	const int nWrittenChars = MultiByteToWideChar(CodePage, 0, sSource.data(), sSource.size(), p.get(), nMemSize);
	return std::wstring(p.get(), nWrittenChars);
}

//------------------------------------------------------

std::string UnicodeToMultibyte(const std::wstring& sSource, const UINT CodePage)
{
	const int nMemSize = WideCharToMultiByte(CodePage, 0, sSource.data(), sSource.size(), NULL, 0, NULL, NULL);
	if(0 == nMemSize)
	{
		return std::string();
	}
	std::array_ptr<char> p(new char[nMemSize]);
	if(p.get() == NULL)
	{
		return std::string();
	}
	const int nWrittenChars = WideCharToMultiByte(CodePage, 0, sSource.data(), sSource.size(), p.get(), nMemSize, NULL, NULL);
	return std::string(p.get(), nWrittenChars);
}

//------------------------------------------------------

std::string TruncateUtf8String(const std::string& sSource, const size_t nMaxChars)
{
	std::wstring sUnicode = Utf8ToUnicode(sSource);
	if(sUnicode.length() > nMaxChars)
	{
		sUnicode = sUnicode.substr(0, nMaxChars);
	}
	return UnicodeToUtf8(sUnicode);
}

//------------------------------------------------------------------------

int nocase_cmp(const std::wstring& s1, const std::wstring& s2)
{
	return lstrcmpiW(s1.c_str(), s2.c_str());
// 	std::wstring::const_iterator it1 = s1.begin();
// 	std::wstring::const_iterator it2 = s2.begin();
//
// 	//has the end of at least one of the strings been reached?
// 	while ( (it1 != s1.end()) && (it2 != s2.end()) )
// 	{
// 		const int c1 = (DWORD)(WCHAR)CharUpperW((LPWSTR)(WCHAR)*it1);
// 		const int c2 = (DWORD)(WCHAR)CharUpperW((LPWSTR)(WCHAR)*it2);
// 		if(c1 != c2) //letters differ?
// 			// return -1 to indicate 'smaller than', 1 otherwise
// 			return (c1 < c2) ? -1 : 1;
// 		//proceed to the next character in each string
// 		++it1;
// 		++it2;
// 	}
// 	const std::wstring::size_type size1 = s1.size();
// 	const std::wstring::size_type size2 = s2.size(); // cache lengths
// 	//return -1,0 or 1 according to strings' lengths
// 	if (size1 == size2)
// 		return 0;
// 	return (size1 < size2) ? -1 : 1;
}

//------------------------------------------------------------------------

int nocase_cmp(const std::string& s1, const std::string& s2)
{
	return nocase_cmp(Utf8ToUnicode(s1), Utf8ToUnicode(s2));
// 	std::string::const_iterator it1 = s1.begin();
// 	std::string::const_iterator it2 = s2.begin();
//
// 	//has the end of at least one of the strings been reached?
// 	while ( (it1 != s1.end()) && (it2 != s2.end()) )
// 	{
// 		const int c1 = ::toupper(*it1);
// 		const int c2 = ::toupper(*it2);
// 		if(c1 != c2) //letters differ?
// 			// return -1 to indicate 'smaller than', 1 otherwise
// 			return (c1 < c2) ? -1 : 1;
// 		//proceed to the next character in each string
// 		++it1;
// 		++it2;
// 	}
// 	std::string::size_type size1=s1.size(), size2=s2.size();// cache lengths
// 	//return -1,0 or 1 according to strings' lengths
// 	if (size1 == size2)
// 		return 0;
// 	return (size1 < size2) ? -1 : 1;
}

//------------------------------------------------------------------------

std::string implode(const std::vector<std::string>& vect, const std::string& sDelim)
{
	std::string s;
//	std::ostringstream stream;	// SK: it is significantly slower than std::string for some reason
	const std::string::size_type n = vect.size();
	for(std::string::size_type i = 0; i < n; i++)
	{
		if(i > 0)
		{
			s += sDelim;
//			stream << sDelim;
		}
		s += vect[i];
//		stream << vect[i];
	}
	return s;
//	return stream.str();
}

std::wstring implode(const std::vector<std::wstring>& vect, const std::wstring& sDelim)
{
	std::wstring s;
//	std::wostringstream stream;	// SK: it is significantly slower than std::string for some reason
	const std::wstring::size_type n = vect.size();
	for(std::wstring::size_type i = 0; i < n; i++)
	{
		if(i > 0)
		{
			s += sDelim;
//			stream << sDelim;
		}
		s += vect[i];
//		stream << vect[i];
	}
	return s;
//	return stream.str();
}

//------------------------------------------------------------------------

void explode(OUT std::vector<std::string>& vect, const std::string& sText, const std::string& sDelim)
{
	vect.clear();

	if(sText.empty())
		return;

	if(sDelim.empty())
	{
		vect.push_back(sText);
		return;
	}

	std::string::size_type pos = 0;
	while(true)
	{
		const std::string::size_type pos2 = sText.find(sDelim, pos);
		if(std::string::npos == pos2)
		{
			vect.push_back(sText.substr(pos));
			break;
		}

		vect.push_back(sText.substr(pos, pos2-pos));
		pos = pos2 + sDelim.length();
	}
}

void explode(OUT std::vector<std::wstring>& vect, const std::wstring& sText, const std::wstring& sDelim)
{
	vect.clear();

	if(sText.empty())
		return;

	if(sDelim.empty())
	{
		vect.push_back(sText);
		return;
	}

	std::wstring::size_type pos = 0;
	while(true)
	{
		const std::wstring::size_type pos2 = sText.find(sDelim, pos);
		if(std::wstring::npos == pos2)
		{
			vect.push_back(sText.substr(pos));
			break;
		}

		vect.push_back(sText.substr(pos, pos2-pos));
		pos = pos2 + sDelim.length();
	}
}

//------------------------------------------------------
