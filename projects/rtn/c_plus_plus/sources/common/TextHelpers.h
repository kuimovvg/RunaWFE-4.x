// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2013
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

unsigned long mystoul(const std::string& s);
unsigned long mystoul(const std::wstring& s);

std::string ToLower(const std::string& s);		// UTF8, using Unicode inside
std::wstring ToLower(const std::wstring& s);	// Unicode
std::string ToUpper(const std::string& s);		// UTF8, using Unicode inside
std::wstring ToUpper(const std::wstring& s);	// Unicode

//char ToLower(const char ch);		// ANSI
wchar_t ToLower(const wchar_t ch);	// Unicode
//char ToUpper(const char ch);		// ANSI
wchar_t ToUpper(const wchar_t ch);	// Unicode

//size_t StrPosI(const std::string& sText, const std::string& sSubstring, const size_t offset = 0);	// ANSI, returns -1 if not found
size_t StrPosI(const std::wstring& sText, const std::wstring& sSubstring, const size_t offset = 0);	// Unicode, returns -1 if not found

size_t StrPosI(const std::string& sText, const char ch, const size_t offset = 0);		// ANSI, returns -1 if not found
size_t StrPosI(const std::wstring& sText, const wchar_t ch, const size_t offset = 0);	// Unicode, returns -1 if not found

int MyStrCmpNI(LPCWSTR p1, LPCWSTR p2, const size_t nMaxChars);	// Unicode

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
int nocase_cmp(const std::string& s1, const std::string& s2);	// utf8

//------------------------------------------------------------------------

inline bool AreStringsEqualNoCase(const std::string& s1, const std::string& s2)	// utf8
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

inline bool beginsi(const std::wstring& s, const std::wstring& begin) // ignore case
{
	//return begins(ToLower(s), ToLower(begin));
	const std::wstring::size_type len = begin.length();
	if(s.length() < len)
		return false;

	return 0 == MyStrCmpNI(s.c_str(), begin.c_str(), len);
}

//------------------------------------------------------------------------

inline bool beginsi(const std::string& s, const std::string& begin) // ignore case, UTF8 (using Unicode inside)
{
	//return begins(ToLower(s), ToLower(begin));
	return beginsi(Utf8ToUnicode(s), Utf8ToUnicode(begin));
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

inline bool endsi(const std::wstring& s, const std::wstring& end) // ignore case
{
	//return ends(ToLower(s), ToLower(end));
	const std::wstring::size_type len = end.length();
	if(s.length() < len)
		return false;

	return 0 == MyStrCmpNI(s.c_str() + s.length() - len, end.c_str(), len);
}

//------------------------------------------------------------------------

inline bool endsi(const std::string& s, const std::string& end) // ignore case, UTF8 (using Unicode inside)
{
	//return ends(ToLower(s), ToLower(end));
	return endsi(Utf8ToUnicode(s), Utf8ToUnicode(end));
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

inline bool containsi(const std::string& str, const std::string& substr) // ignore case, UTF8 (using Unicode inside)
{
	//return contains(ToLower(str), ToLower(substr));
	return StrPosI(Utf8ToUnicode(str), Utf8ToUnicode(substr)) != -1;
}

//------------------------------------------------------------------------

inline bool containsi(const std::wstring& str, const std::wstring& substr) // ignore case
{
	//return contains(ToLower(str), ToLower(substr));
	return StrPosI(str, substr) != -1;
}

//------------------------------------------------------------------------

inline bool containsi(const std::string& str, const char ch) // ignore case, ANSI
{
	//return contains(ToLower(str), ToLower(ch));
	return StrPosI(str, ch) != -1;
}

//------------------------------------------------------------------------

inline bool containsi(const std::wstring& str, const wchar_t ch) // ignore case
{
	//return contains(ToLower(str), ToLower(ch));
	return StrPosI(str, ch) != -1;
}

//------------------------------------------------------------------------

inline std::string ExpandStringByOuterBorders(const std::string& sText,
	const std::string& sBeforeSubstr, const std::string& sAfterSubstr, const std::string& sDefaultValue = "")
{
	size_t pos1 = sText.find(sBeforeSubstr);
	if(pos1 == std::string::npos)
		return sDefaultValue;
	pos1 += sBeforeSubstr.length();
	const size_t pos2 = sText.find(sAfterSubstr, pos1);
	if(pos2 == std::string::npos)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1);
}

inline std::wstring ExpandStringByOuterBorders(const std::wstring& sText,
	const std::wstring& sBeforeSubstr, const std::wstring& sAfterSubstr, const std::wstring& sDefaultValue = L"")
{
	size_t pos1 = sText.find(sBeforeSubstr);
	if(pos1 == std::wstring::npos)
		return sDefaultValue;
	pos1 += sBeforeSubstr.length();
	const size_t pos2 = sText.find(sAfterSubstr, pos1);
	if(pos2 == std::wstring::npos)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1);
}

//------------------------------------------------------

inline std::wstring ExpandStringByOuterBordersI(const std::wstring& sText,
	const std::wstring& sBeforeSubstr, const std::wstring& sAfterSubstr, const std::wstring& sDefaultValue = L"") // ignore case
{
	size_t pos1 = StrPosI(sText, sBeforeSubstr);
	if(pos1 == -1)
		return sDefaultValue;
	pos1 += sBeforeSubstr.length();
	const size_t pos2 = StrPosI(sText, sAfterSubstr, pos1);
	if(pos2 == -1)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1);
}

//------------------------------------------------------------------------

inline std::string ExpandStringByInnerBorders(const std::string& sText,
	const std::string& sBeginSubst, const std::string& sEndSubstr, const std::string& sDefaultValue = "")
{
	const size_t pos1 = sText.find(sBeginSubst);
	if(pos1 == std::string::npos)
		return sDefaultValue;
	const size_t pos2 = sText.find(sEndSubstr, pos1 + sBeginSubst.length());
	if(pos2 == std::string::npos)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1 + sEndSubstr.length());
}

inline std::wstring ExpandStringByInnerBorders(const std::wstring& sText,
	const std::wstring& sBeginSubst, const std::wstring& sEndSubstr, const std::wstring& sDefaultValue = L"")
{
	const size_t pos1 = sText.find(sBeginSubst);
	if(pos1 == std::wstring::npos)
		return sDefaultValue;
	const size_t pos2 = sText.find(sEndSubstr, pos1 + sBeginSubst.length());
	if(pos2 == std::wstring::npos)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1 + sEndSubstr.length());
}

//------------------------------------------------------------------------

inline std::wstring ExpandStringByInnerBordersI(const std::wstring& sText,
	const std::wstring& sBeginSubst, const std::wstring& sEndSubstr, const std::wstring& sDefaultValue = L"")
{
	const size_t pos1 = StrPosI(sText, sBeginSubst);
	if(pos1 == -1)
		return sDefaultValue;
	const size_t pos2 = StrPosI(sText, sEndSubstr, pos1 + sBeginSubst.length());
	if(pos2 == -1)
		return sDefaultValue;
	return sText.substr(pos1, pos2 - pos1 + sEndSubstr.length());
}

//------------------------------------------------------------------------

void BreakStringToTokens(OUT std::vector<tstring>& vectTokens, const tstring& sText, const tstring& sSeparators);

// Note! BreakStringToLines() is a bit different from BreakToTokens() because it can return empty strings!
void BreakStringToLines(OUT std::vector<std::string>& vectLines, const std::string& sText);
void BreakStringToLines(OUT std::vector<std::wstring>& vectLines, const std::wstring& sText);

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
