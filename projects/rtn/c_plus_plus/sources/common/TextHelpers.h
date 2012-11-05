// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#pragma once

#include <stdio.h>
#include <stdarg.h>

#include <string>
#include <vector>

#include "basedef.h"

#ifdef UNITTEST_DEPENDENCY
#include "Modules.h"
LINK_TO_MODULE(TextHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

#ifdef COMPILE_MODULE_INFO
#include "timestamp.h"
#ifndef MODULE_TIME_STAMP_USED
#define MODULE_TIME_STAMP_USED
MODULE_TIME_STAMP
#endif
#endif //ifdef COMPILE_MODULE_INFO

//------------------------------------------------------------------------

namespace StdString
{
	std::string Format(LPCSTR pszFormat, ...);
	std::wstring FormatW(LPCWSTR pszFormat, ...);

	std::string FormatV(LPCSTR pszFormat, va_list argList);
	std::wstring FormatVW(LPCWSTR pszFormat, va_list argList);

	tstring FormatApiError(LONG errorCode); // Formatting trims \n at the end of string!
	std::string FormatApiErrorA(LONG errorCode); // Formatting trims \n at the end of string!
	std::wstring FormatApiErrorW(LONG errorCode); // Formatting trims \n at the end of string!
};

tstring itot(int x); // converts int to string
tstring ltot(long x); // converts long to string
tstring ultot(unsigned long x); // converts unsigned long to string

unsigned long atoul(const char* s);

std::string ToLower(const std::string& s);
std::wstring ToLower(const std::wstring& s);
std::string ToUpper(const std::string& s);
std::wstring ToUpper(const std::wstring& s);

char ToLower(const char ch);
wchar_t ToLower(const wchar_t ch);
char ToUpper(const char ch);
wchar_t ToUpper(const wchar_t ch);

//------------------------------------------------------------------------

template<class T>
inline void findandreplace(T& source, const T& find, const T& replace)
{
	if(find.empty())
		return;

	T::size_type pos = 0;
	while((pos = source.find(find, pos)) != T::npos)
	{
		source.replace(pos, find.length(), replace);
		pos += replace.length();
	}
}

inline void findandreplace(std::string& source, LPCSTR find, LPCSTR replace)
{
	::findandreplace(source, (std::string)find, (std::string)replace);
}

inline void findandreplace(std::wstring& source, LPCWSTR find, LPCWSTR replace)
{
	::findandreplace(source, (std::wstring)find, (std::wstring)replace);
}

//------------------------------------------------------------------------

inline std::string lf2crlf(const std::string& source)
{
	std::string s = source;
	findandreplace(s, "\n", "\r\n");
	return s;
}

inline std::wstring lf2crlf(const std::wstring& source)
{
	std::wstring s = source;
	findandreplace(s, L"\n", L"\r\n");
	return s;
}

//------------------------------------------------------

std::wstring MultibyteToUnicode(const std::string& sSource, const UINT CodePage);
std::string UnicodeToMultibyte(const std::wstring& sSource, const UINT CodePage);

inline std::wstring Utf8ToUnicode(const std::string& sSource)
{
	return MultibyteToUnicode(sSource, CP_UTF8);
}

inline std::string UnicodeToUtf8(const std::wstring& sSource)
{
	return UnicodeToMultibyte(sSource, CP_UTF8);
}

std::string TruncateUtf8String(const std::string& sSource, const size_t nMaxChars);

//------------------------------------------------------------------------

inline std::wstring StringToUnicode(const std::wstring& s)
{
	return s;
}

inline std::wstring StringToUnicode(const std::string& s)
{
	return Utf8ToUnicode(s);
}

//------------------------------------------------------------------------

inline tstring UnicodeToString(const std::wstring& s)
{
#ifdef UNICODE
	return s;
#else
	return UnicodeToUtf8(s);
#endif // !UNICODE
}

//------------------------------------------------------------------------

inline std::string StringToUtf8(const std::wstring& s)
{
	return UnicodeToUtf8(s);
}

inline std::string StringToUtf8(const std::string& s)
{
	return s;
}

//------------------------------------------------------------------------

inline tstring Utf8ToString(const std::string& s)
{
#ifdef UNICODE
	return Utf8ToUnicode(s);
#else
	return s;
#endif // !UNICODE
}

//------------------------------------------------------------------------

int nocase_cmp(const std::wstring& s1, const std::wstring& s2);
int nocase_cmp(const std::string& s1, const std::string& s2);

//------------------------------------------------------------------------

inline bool AreStringsEqualNoCase(const std::string& s1, const std::string& s2)
{
	return 0 == nocase_cmp(s1, s2);
}

inline bool AreStringsEqualNoCase(const std::wstring& s1, const std::wstring& s2)
{
	return 0 == nocase_cmp(s1, s2);
}

//------------------------------------------------------------------------

inline std::string trim_left(const std::string& s, const std::string& drop = " \t\r\n")
{
	const std::string::size_type pos = s.find_first_not_of(drop);
	if(std::string::npos == pos)
	{
		return "";
	}
	else
	{
		return s.substr(pos);
	}
}

inline std::wstring trim_left(const std::wstring& s, const std::wstring& drop = L" \t\r\n")
{
	const std::wstring::size_type pos = s.find_first_not_of(drop);
	if(std::wstring::npos == pos)
	{
		return L"";
	}
	else
	{
		return s.substr(pos);
	}
}

//------------------------------------------------------------------------

inline std::string trim_right(const std::string& s, const std::string& drop = " \t\r\n")
{
	const std::string::size_type pos = s.find_last_not_of(drop);
	if(std::string::npos == pos)
	{
		return "";
	}
	else
	{
		return s.substr(0, pos+1);
	}
}

inline std::wstring trim_right(const std::wstring& s, const std::wstring& drop = L" \t\r\n")
{
	const std::wstring::size_type pos = s.find_last_not_of(drop);
	if(std::wstring::npos == pos)
	{
		return L"";
	}
	else
	{
		return s.substr(0, pos+1);
	}
}

//------------------------------------------------------------------------

inline std::string trim(const std::string& s, const std::string& drop = " \t\r\n") // trim left & right
{
	const std::string::size_type posBegin = s.find_first_not_of(drop);
	if(std::string::npos == posBegin)
	{
		return "";
	}
	const std::string::size_type posEnd = s.find_last_not_of(drop);
	const std::string::size_type length = s.length();
	if(posBegin == 0 && posEnd == length-1)
	{
		return s;
	}
	if(posBegin == 0)
	{
		return s.substr(0, posEnd+1);
	}
	return s.substr(posBegin, posEnd+1 - posBegin);
}

inline std::wstring trim(const std::wstring& s, const std::wstring& drop = L" \t\r\n") // trim left & right
{
	const std::wstring::size_type posBegin = s.find_first_not_of(drop);
	if(std::wstring::npos == posBegin)
	{
		return L"";
	}
	const std::wstring::size_type posEnd = s.find_last_not_of(drop);
	const std::wstring::size_type length = s.length();
	if(posBegin == 0 && posEnd == length-1)
	{
		return s;
	}
	if(posBegin == 0)
	{
		return s.substr(0, posEnd+1);
	}
	return s.substr(posBegin, posEnd+1 - posBegin);
}

//------------------------------------------------------------------------

inline bool begins(const std::string& s, const std::string& begin)
{
	const std::string::size_type len = begin.length();
	if(s.length() < len)
		return false;

	return 0 == strncmp(s.c_str(), begin.c_str(), len);
}

//------------------------------------------------------------------------

inline bool begins(const std::wstring& s, const std::wstring& begin)
{
	const std::wstring::size_type len = begin.length();
	if(s.length() < len)
		return false;

	return 0 == wcsncmp(s.c_str(), begin.c_str(), len);
}

//------------------------------------------------------------------------

inline bool beginsi(const std::string& s, const std::string& begin) // ignore case
{
	return begins(ToLower(s), ToLower(begin));
}

//------------------------------------------------------------------------

inline bool beginsi(const std::wstring& s, const std::wstring& begin) // ignore case
{
	return begins(ToLower(s), ToLower(begin));
}

//------------------------------------------------------------------------

inline bool ends(const std::string& s, const std::string& end)
{
	const std::string::size_type len = end.length();
	if(s.length() < len)
		return false;

	return 0 == strncmp(s.c_str() + s.length() - len, end.c_str(), len);
}

//------------------------------------------------------------------------

inline bool ends(const std::wstring& s, const std::wstring& end)
{
	const std::wstring::size_type len = end.length();
	if(s.length() < len)
		return false;

	return 0 == wcsncmp(s.c_str() + s.length() - len, end.c_str(), len);
}

//------------------------------------------------------------------------

inline bool endsi(const std::string& s, const std::string& end) // ignore case
{
	return ends(ToLower(s), ToLower(end));
}

//------------------------------------------------------------------------

inline bool endsi(const std::wstring& s, const std::wstring& end) // ignore case
{
	return ends(ToLower(s), ToLower(end));
}

//------------------------------------------------------------------------

inline bool contains(const std::string& str, const std::string& substr)
{
	return str.find(substr) != std::string::npos;
}

//------------------------------------------------------------------------

inline bool contains(const std::wstring& str, const std::wstring& substr)
{
	return str.find(substr) != std::wstring::npos;
}

//------------------------------------------------------------------------

inline bool contains(const std::string& str, const char ch)
{
	return str.find(ch) != std::string::npos;
}

//------------------------------------------------------------------------

inline bool contains(const std::wstring& str, const wchar_t ch)
{
	return str.find(ch) != std::wstring::npos;
}

//------------------------------------------------------------------------

inline bool containsi(const std::string& str, const std::string& substr) // ignore case
{
	return contains(ToLower(str), ToLower(substr));
}

//------------------------------------------------------------------------

inline bool containsi(const std::wstring& str, const std::wstring& substr) // ignore case
{
	return contains(ToLower(str), ToLower(substr));
}

//------------------------------------------------------------------------

inline bool containsi(const std::string& str, const char ch) // ignore case
{
	return contains(ToLower(str), ToLower(ch));
}

//------------------------------------------------------------------------

inline bool containsi(const std::wstring& str, const wchar_t ch) // ignore case
{
	return contains(ToLower(str), ToLower(ch));
}

//------------------------------------------------------------------------

inline std::string ExpandStringByOuterBorders(const std::string& sText, const std::string& sBeforeSubstr, const std::string& sAfterSubstr)
{
	size_t pos1 = sText.find(sBeforeSubstr);
	if(pos1 == std::string::npos)
		return "";
	pos1 += sBeforeSubstr.length();
	const size_t pos2 = sText.find(sAfterSubstr, pos1);
	if(pos2 == std::string::npos)
		return "";
	return sText.substr(pos1, pos2 - pos1);
}

inline std::wstring ExpandStringByOuterBorders(const std::wstring& sText, const std::wstring& sBeforeSubstr, const std::wstring& sAfterSubstr)
{
	size_t pos1 = sText.find(sBeforeSubstr);
	if(pos1 == std::wstring::npos)
		return L"";
	pos1 += sBeforeSubstr.length();
	const size_t pos2 = sText.find(sAfterSubstr, pos1);
	if(pos2 == std::wstring::npos)
		return L"";
	return sText.substr(pos1, pos2 - pos1);
}

//------------------------------------------------------------------------

inline std::string ExpandStringByInnerBorders(const std::string& sText, const std::string& sBeginSubst, const std::string& sEndSubstr)
{
	const size_t pos1 = sText.find(sBeginSubst);
	if(pos1 == std::string::npos)
		return "";
	const size_t pos2 = sText.find(sEndSubstr, pos1 + sBeginSubst.length());
	if(pos2 == std::string::npos)
		return "";
	return sText.substr(pos1, pos2 - pos1 + sEndSubstr.length());
}

inline std::wstring ExpandStringByInnerBorders(const std::wstring& sText, const std::wstring& sBeginSubst, const std::wstring& sEndSubstr)
{
	const size_t pos1 = sText.find(sBeginSubst);
	if(pos1 == std::wstring::npos)
		return L"";
	const size_t pos2 = sText.find(sEndSubstr, pos1 + sBeginSubst.length());
	if(pos2 == std::wstring::npos)
		return L"";
	return sText.substr(pos1, pos2 - pos1 + sEndSubstr.length());
}

//------------------------------------------------------------------------

void BreakStringToTokens(OUT std::vector<tstring>& vectTokens, const tstring& sText, const tstring& sSeparators);

// Note! BreakStringToLines() is a bit different from BreakToTokens() because it can return empty strings!
void BreakStringToLines(OUT std::vector<tstring>& vectLines, const tstring& sText);

bool BreakStringToKeyValue(const std::string& sText, OUT std::string& sKey, OUT std::string& sValue,
	const std::string& sEqualStr = "=", const std::string& sTrimChars = " \t\r\n");

bool BreakStringToKeyValue(const std::wstring& sText, OUT std::wstring& sKey, OUT std::wstring& sValue,
	const std::wstring& sEqualStr = L"=", const std::wstring& sTrimChars = L" \t\r\n");

//------------------------------------------------------------------------

std::string implode(const std::vector<std::string>& vect, const std::string& sDelim = ", ");
std::wstring implode(const std::vector<std::wstring>& vect, const std::wstring& sDelim = L", ");

// Note! explode() is a bit different from BreakToTokens() because it breaks by ONE full-text delimiter!

void explode(OUT std::vector<std::string>& vect, const std::string& sText, const std::string& sDelim = ", ");
void explode(OUT std::vector<std::wstring>& vect, const std::wstring& sText, const std::wstring& sDelim = L", ");

inline std::vector<std::string> explode(const std::string& sText, const std::string& sDelim = ", ")
{
	std::vector<std::string> vect;
	explode(vect, sText, sDelim);
	return vect;
}

inline std::vector<std::wstring> explode(const std::wstring& sText, const std::wstring& sDelim = L", ")
{
	std::vector<std::wstring> vect;
	explode(vect, sText, sDelim);
	return vect;
}

template<class T>
inline void AppendVector(OUT std::vector<T>& dest, const std::vector<T>& src)
{
	dest.insert(dest.end(), src.begin(), src.end());
}

//------------------------------------------------------------------------
