// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------------------------

#include "stdafx.h"
#include "TimeHelpers.h"

#include "Tracing.h"

//#include "memory.h"
#define MAKE_DWORD(a,b,c,d)	MAKELONG(MAKEWORD(a,b),MAKEWORD(c,d))	// MAKE: 0xdcba, 'dcba'

#ifdef UNITTEST_DEPENDENCY
#include "UnitTest\UnitTest.h"
IMPLEMENT_MODULE(TimeHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

TimeZone::TimeZone()
{
	_tzset();
	timezone = 0;
	daylight = 1;
	daylightoffset = -3600;
#if _MSC_VER <= 1200
	timezone = _timezone;
	daylight = _daylight;
	daylightoffset = _dstbias;
#else
	_get_timezone(&timezone);
	_get_daylight(&daylight);
	_get_dstbias(&daylightoffset);
#endif
}

TimeZone g_TimeZoneSet;

//------------------------------------------------------

bool IsLeapYear(const int nYear)
{
	if(nYear % 4000 == 0)	// this is too rare event, so may be commented to make code faster
		return false;
	if(nYear % 400 == 0)
		return true;
	if(nYear % 100 == 0)
		return false;
	if(nYear % 4 == 0)
		return true;
	return false;
}

//------------------------------------------------------

const LPCSTR g_szMonths[12] =
{
	"January",
	"February",
	"March",
	"April",
	"May",
	"June",
	"July",
	"August",
	"September",
	"October",
	"November",
	"December"
};

const LPCSTR g_szWeekDays[7] =
{
	"Sunday",
	"Monday",
	"Tuesday",
	"Wednesday",
	"Thursday",
	"Friday",
	"Saturday"
};

//------------------------------------------------------

std::string FormatMonth(const int nMonth)	// "January" - 1, "February" - 2, "March" - 3, ...
{
	if(nMonth < 1 || nMonth > 12)
		return "";
	return g_szMonths[nMonth-1];
}

//------------------------------------------------------

std::string FormatShortMonth(const int nMonth)	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...
{
	if(nMonth < 1 || nMonth > 12)
		return "";
	return std::string(g_szMonths[nMonth-1], 3);
}

//------------------------------------------------------

std::string FormatWeekday(const int nWeekday)	// "Sunday" = 0, "Monday" - 1, "Tuesday" - 2, ...
{
	if(nWeekday < 0 || nWeekday > 6)
		return "";
	return g_szWeekDays[nWeekday];
}

//------------------------------------------------------

std::string FormatShortWeekday(const int nWeekday)	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...
{
	if(nWeekday < 0 || nWeekday > 6)
		return "";
	return std::string(g_szWeekDays[nWeekday], 3);
}

//------------------------------------------------------

int ParseShortWeekDay(const char ch1, const char ch2, const char ch3)	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...
{
	const DWORD dw = MAKE_DWORD(ch3, ch2, ch1, 0); // 'xyz' == '\0xyz' (NOT 'xyz\0')
	switch(dw)
	{
	case 'Sun': return 0;
	case 'Mon': return 1;
	case 'Tue': return 2;
	case 'Wed': return 3;
	case 'Thu': return 4;
	case 'Fri': return 5;
	case 'Sat': return 6;
	}
	MYTRACE("ERROR: Unknown short week day: '%c%c%c'\n", ch1, ch2, ch3);
	MYASSERT(FALSE && "Unknown short week day!");
	return 0;
}

//------------------------------------------------------

int ParseShortMonthName(const char ch1, const char ch2, const char ch3)	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...
{
	const DWORD dw = MAKE_DWORD(ch3, ch2, ch1, 0); // 'xyz' == '\0xyz' (NOT 'xyz\0')
#ifdef _DEBUG
	const WORD dwSe = 'Se';
	const DWORD dwSep = 'Sep';
	const DWORD dwSept = 'Sept';
#endif
	switch(dw)
	{
	case 'Jan': return 1;
	case 'Feb': return 2;
	case 'Mar': return 3;
	case 'Apr': return 4;
	case 'May': return 5;
	case 'Jun': return 6;
	case 'Jul': return 7;
	case 'Aug': return 8;
	case 'Sep': return 9;
	case 'Oct': return 10;
	case 'Nov': return 11;
	case 'Dec': return 12;
	}
	MYTRACE("ERROR: Unknown short month: '%c%c%c'\n", ch1, ch2, ch3);
	MYASSERT(FALSE && "Unknown short month!");
	return 0;
}

//------------------------------------------------------

int ParseTimeZone(const std::string& sZone) // RFC 822, improved military zones, returns munutes offset from GMT
{
//	"((?-i)GMT|UT|PST|PDT|MST|MDT|CST|CDT|EST|EDT|[A-IK-Z]|[+-]\\d{4})"
	if(!sZone.empty())
	{
		const char chFirst = sZone[0];
		if(chFirst == '+' || chFirst == '-')
		{
			const int x = atoi(sZone.substr(1).c_str());
			const int nMinutes = 60 * (x/100) + x%100;
			return chFirst == '-' ? -nMinutes : nMinutes;
		}
		if(sZone.length() == 1)
		{
			// Military time zone
			// RFC822:  Z = UT;  A:-1; (J not used);  M:-12; N:+1; Y:+12
			// RFC1123: The military time zones are specified incorrectly in RFC-822:
			//		they count the wrong way from UT (the signs are reversed).  As
			//		a result, military time zones in RFC-822 headers carry no information.

			// Currently we do not support military time zones!
		}

		if(sZone.length() == 3)
		{
			const DWORD dw = MAKE_DWORD(sZone[2], sZone[1], sZone[0], 0); // 'xyz' == '\0xyz' (NOT 'xyz\0')
			switch(dw)
			{
			case 'GMT': return 0;
			case 'PST': return -8*60;
			case 'PDT': return -7*60;
			case 'MST': return -7*60;
			case 'MDT': return -6*60;
			case 'CST': return -6*60;
			case 'CDT': return -5*60;
			case 'EST': return -5*60;
			case 'EDT': return -4*60;
			}
		}
		if(sZone == "UT")
		{
			return 0;
		}
	}

	MYTRACE("ERROR: Unknown time zone: '%s'\n", sZone.c_str());
	MYASSERT(FALSE && "Unknown time zone! (Ignore == decode as GMT)");
	return 0;
}

//------------------------------------------------------

int StringDateToDaysAgo(const std::string& sDate, const SYSTEMTIME& stNow)
{
	if(sDate.empty())
	{
		return 9999;
	}

	int nYear = 0;
	int nMonth = 0;
	int nDay = 0;

#if _MSC_VER <= 1200
	sscanf(sDate.c_str(), "%04d-%02d-%02d", &nYear, &nMonth, &nDay);
#else
	sscanf_s(sDate.c_str(), "%04d-%02d-%02d", &nYear, &nMonth, &nDay);
#endif

	//	SYSTEMTIME stNow = CurrentTimeAsST();
	SYSTEMTIME st = stNow;
	st.wYear = nYear;
	st.wMonth = nMonth;
	st.wDay = nDay;

	const INT64 day = GetFileTimeDayTicks();
	const INT64 nDelta = ST2LI(stNow).QuadPart - ST2LI(st).QuadPart;
	return int(nDelta / day);
}

//------------------------------------------------------

SYSTEMTIME ParseDbDateTime(const std::string& sDateTime)	// "YYYY-MM-DD HH:MM:SS"
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));

	if(sDateTime.empty())
	{
		return st;
	}

	int nYear = 0;
	int nMonth = 0;
	int nDay = 0;
	int nHour = 0;
	int nMinute = 0;
	int nSecond = 0;

#if _MSC_VER <= 1200
	sscanf(sDateTime.c_str(), "%04d-%02d-%02d %02d:%02d:%02d",
		&nYear, &nMonth, &nDay, &nHour, &nMinute, &nSecond);
#else
	sscanf_s(sDateTime.c_str(), "%04d-%02d-%02d %02d:%02d:%02d",
		&nYear, &nMonth, &nDay, &nHour, &nMinute, &nSecond);
#endif

	st.wYear = nYear;
	st.wMonth = nMonth;
	st.wDay = nDay;
	st.wHour = nHour;
	st.wMinute = nMinute;
	st.wSecond = nSecond;
	return st;
}

//------------------------------------------------------
// ANSI C's asctime() format
// __TIMESTAMP__, The date and time of the last modification of the current source file.
// 'Ddd Mmm Date hh:mm:ss yyyy', where Ddd is the abbreviated day of the week and Date is an integer from 1 to 31
// Example: "Fri Sep  4 16:01:05 2009"

std::string FormatAnsiTimeStamp(const SYSTEMTIME& st)
{
	return StdString::Format("%s %s %2d %02d:%02d:%02d %04d",
		FormatShortWeekday(st.wDayOfWeek).c_str(), FormatShortMonth(st.wMonth).c_str(),
		st.wDay, st.wHour, st.wMinute, st.wSecond, st.wYear);
}

tm ParseAnsiTimeStamp(const std::string& sTimeStamp)
{
	tm t;
	memset(&t, 0, sizeof(t));

	const size_t len = sTimeStamp.length();
	if(0 == len)
		return t;

	if(len != 24)
	{
		//MYTRACE("ERROR: Invalid ANSI timestamp length: %d ('%s')\n", len, sTimeStamp.c_str());
		//MYASSERT(FALSE && "Invalid ANSI timestamp length!");
		return t;
	}

	if(sTimeStamp[3] != ' ' || sTimeStamp[7] != ' '
		|| sTimeStamp[10] != ' ' || sTimeStamp[19] != ' '
		|| sTimeStamp[13] != ':' || sTimeStamp[16] != ':')
	{
		//MYTRACE("ERROR: Invalid ANSI timestamp: '%s'\n", sTimeStamp.c_str());
		//MYASSERT(FALSE && "Invalid ANSI timestamp!");
		return t;
	}

	t.tm_year = atoi(sTimeStamp.c_str() + 20) - 1900;
	t.tm_mon = ParseShortMonthName(sTimeStamp[4], sTimeStamp[5], sTimeStamp[6]) - 1;
	t.tm_mday = atoi(sTimeStamp.c_str() + 8);
	t.tm_hour = atoi(sTimeStamp.c_str() + 11);
	t.tm_min = atoi(sTimeStamp.c_str() + 14);
	t.tm_sec = atoi(sTimeStamp.c_str() + 17);
	t.tm_wday = ParseShortWeekDay(sTimeStamp[0], sTimeStamp[1], sTimeStamp[2]);
	return t;
}

//------------------------------------------------------
// Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123

std::string FormatRFC822TimeStamp(const SYSTEMTIME& st)
{
	return StdString::Format("%s, %02d %s %04d %02d:%02d:%02d GMT",
		FormatShortWeekday(st.wDayOfWeek).c_str(),
		st.wDay, FormatShortMonth(st.wMonth).c_str(), st.wYear,
		st.wHour, st.wMinute, st.wSecond);
}
/*
tm ParseRFC822TimeStamp(const std::string& sTimeStamp)
{
	tm t;
	memset(&t, 0, sizeof(t));

	const size_t len = sTimeStamp.length();
	if(0 == len)
		return t;

	const size_t pos = sTimeStamp.find(',');
	if(len != pos + 26)	// len == 27
	{
		MYTRACE("ERROR: Invalid RFC822 timestamp length: %d ('%s')\n", len, sTimeStamp.c_str());
		MYASSERT(FALSE && "Invalid RFC822 timestamp length!");
		return t;
	}

	if(!( (pos == 3)
		&& (sTimeStamp[pos + 1] == ' ')
		&& (sTimeStamp[pos + 4] == ' ')
		&& (sTimeStamp[pos + 8] == ' ')
		&& (sTimeStamp[pos + 13] == ' ')
		&& (sTimeStamp[pos + 22] == ' ')
		&& (sTimeStamp[pos + 16] == ':')
		&& (sTimeStamp[pos + 19] == ':')
		&& (sTimeStamp[pos + 23] == 'G')
		&& (sTimeStamp[pos + 24] == 'M')
		&& (sTimeStamp[pos + 25] == 'T') ) )
	{
		MYTRACE("ERROR: Incorrect RFC822 timestamp format: '%s'\n", sTimeStamp.c_str());
		MYASSERT(FALSE && "Incorrect RFC822 timestamp format!");
		return t;
	}

	t.tm_year = atoi(sTimeStamp.c_str() + pos + 9) - 1900;
	t.tm_mon = ParseShortMonthName(sTimeStamp[pos + 5], sTimeStamp[pos + 6], sTimeStamp[pos + 7]) - 1;
	t.tm_mday = atoi(sTimeStamp.c_str() + pos + 2);
	t.tm_hour = atoi(sTimeStamp.c_str() + pos + 14);
	t.tm_min = atoi(sTimeStamp.c_str() + pos + 17);
	t.tm_sec = atoi(sTimeStamp.c_str() + pos + 20);
	t.tm_wday = ParseShortWeekDay(sTimeStamp[0], sTimeStamp[1], sTimeStamp[2]);
	return t;
}
*/
//------------------------------------------------------------------------
// Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
// Sunday, 06-Nov-2005 08:49:37 GMT	- found
// Sunday, 6-Nov-2005 08:49:37 GMT	- found

std::string FormatRFC850TimeStamp(const SYSTEMTIME& st)
{
	return StdString::Format("%s, %02d-%s-%02d %02d:%02d:%02d GMT",
		FormatWeekday(st.wDayOfWeek).c_str(),
		st.wDay, FormatShortMonth(st.wMonth).c_str(), st.wYear % 100,
		st.wHour, st.wMinute, st.wSecond);
}

//------------------------------------------------------

// __DATE__, The compilation date of the current source file.
// 'Mmm dd yyyy'. The month name Mmm is the same as for dates generated by the library function asctime declared in TIME.H.
// Example: "Sep  4 2009"
// __TIME__, The most recent compilation time of the current source file.
// 'hh:mm:ss'
// Example: "16:01:05"

tm ParseDateAndTime(const std::string& sDate, const std::string& sTime)
{
	tm t;
	memset(&t, 0, sizeof(t));

	{
		const size_t len = sDate.length();
		if(len > 0)
		{
			if(len != 11)
			{
				MYTRACE("ERROR: Invalid date length: %d ('%s')\n", len, sDate.c_str());
				MYASSERT(FALSE && "Invalid date length!");
			}
			t.tm_year = atoi(sDate.c_str() + 7) - 1900;
			t.tm_mon = ParseShortMonthName(sDate[0], sDate[1], sDate[2]) - 1;
			t.tm_mday = atoi(sDate.c_str() + 4);
		}
	}

	{
		const size_t len = sTime.length();
		if(len > 0)
		{
			if(len != 8)
			{
				MYTRACE("ERROR: Invalid time length: %d ('%s')\n", len, sTime.c_str());
				MYASSERT(FALSE && "Invalid time length!");
			}
			t.tm_hour = atoi(sTime.c_str() + 0);
			t.tm_min = atoi(sTime.c_str() + 3);
			t.tm_sec = atoi(sTime.c_str() + 6);
		}
	}
	return t;
}

//------------------------------------------------------------------------
//------------------------------------------------------------------------
//------------------------------------------------------------------------
//------------------------------------------------------------------------
