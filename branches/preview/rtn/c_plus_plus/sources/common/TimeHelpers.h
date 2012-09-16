// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_TIMEHELPERS_H__5F43BC3F_FEC8_4718_88AD_455255DA8CCF__INCLUDED_)
#define AFX_TIMEHELPERS_H__5F43BC3F_FEC8_4718_88AD_455255DA8CCF__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "TextHelpers.h"
#include <memory.h>

//#include "memory.h"
inline bool IsZeroMemory(const void* p, const size_t nBytes)
{
	const char* pc = (const char*)p;
	for(size_t i = 0; i < nBytes; i++)
	{
		if((*pc++) != 0)
			return false;
	}
	return true;
}


//#define _USE_32BIT_TIME_T
#include <time.h>

#ifdef UNITTEST_DEPENDENCY
#include "Modules.h"
LINK_TO_MODULE(TimeHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------------------------

// UnixTime: (32bits) seconds since midnight, January 1, 1970
// FILETIME: (64bits) the number of 100-nanosecond units since the beginning of January 1, 1601
// LARGE_INTEGER: same as FILETIME

//------------------------------------------------------------------------

inline INT64 GetFileTimeSecondTicks()
{
	const INT64 day = 10000I64 * 1000;
	return day;
}

//------------------------------------------------------------------------

inline INT64 GetSecondsBetweenUnixTimeAndFileTime()
{
	// There were 360 years between 1 Jan 1601 and 1 Jan 1961
	// 360 / 4 = 90 leap years.
	// But 1700, 1800, 1900 were no leap year (they are be divided by 100, but not 400) (-3 leap years)
	// And 1964, 1968 were leap years after 1961. (+2 leap years)
	const int nLeapYearsBetweenUnixTimeAndFileTime = 90 - 3 + 2;
	const int nDaysBetweenUnixTimeAndFileTime = (1970 - 1601) * 365 + nLeapYearsBetweenUnixTimeAndFileTime;
	const INT64 nSecondsBetweenUnixTimeAndFileTime = nDaysBetweenUnixTimeAndFileTime * 3600I64 * 24;
	return nSecondsBetweenUnixTimeAndFileTime;
}

//------------------------------------------------------------------------

inline INT64 GetFileTimeDayTicks()
{
	const INT64 day = 10000I64 * 1000 * 3600 * 24;
	return day;
}

//------------------------------------------------------------------------

inline LARGE_INTEGER FT2LI(const FILETIME& ft)
{
	LARGE_INTEGER li;
	memset(&li, 0, sizeof(li));
	li.LowPart = ft.dwLowDateTime;
	li.HighPart = ft.dwHighDateTime;
	return li;
}

//------------------------------------------------------------------------

inline FILETIME LI2FT(const LARGE_INTEGER& li)
{
	FILETIME ft;
	memset(&ft, 0, sizeof(ft));
	ft.dwLowDateTime = li.LowPart;
	ft.dwHighDateTime = li.HighPart;
	return ft;
}

//------------------------------------------------------------------------

inline LARGE_INTEGER ST2LI(const SYSTEMTIME& st)
{
	FILETIME ft;
	memset(&ft, 0, sizeof(ft));
	if(IsZeroMemory(&st, sizeof(st)))
		return FT2LI(ft);
	SystemTimeToFileTime(&st, &ft);
	return FT2LI(ft);
}

//------------------------------------------------------------------------

inline SYSTEMTIME LI2ST(const LARGE_INTEGER& li)
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));
	if(IsZeroMemory(&li, sizeof(li)))
		return st;
	const FILETIME ft = LI2FT(li);
	FileTimeToSystemTime(&ft, &st);
	return st;
}

//------------------------------------------------------------------------

inline time_t LI2TIMET(const LARGE_INTEGER& li)
{
	if(li.QuadPart == 0)
		return 0;
	const INT64 unixtime = (li.QuadPart / GetFileTimeSecondTicks()) - GetSecondsBetweenUnixTimeAndFileTime();
	return (time_t)unixtime;
}

//------------------------------------------------------------------------

inline LARGE_INTEGER TIMET2LI(const time_t& t)
{
	LARGE_INTEGER li;
	memset(&li, 0, sizeof(li));
	if(t == 0)
		return li;
	li.QuadPart = (t + GetSecondsBetweenUnixTimeAndFileTime()) * GetFileTimeSecondTicks();
	return li;
}

//------------------------------------------------------------------------

inline SYSTEMTIME TM2ST(const tm& t)
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));
	if(IsZeroMemory(&t, sizeof(t)))
		return st;
	st.wYear = t.tm_year + 1900;
	st.wMonth = t.tm_mon + 1;
	st.wDayOfWeek = t.tm_wday;
	st.wDay = t.tm_mday;
	st.wHour = t.tm_hour;
	st.wMinute = t.tm_min;
	st.wSecond = t.tm_sec;
	st.wMilliseconds = 0;
	return st;
}

//------------------------------------------------------------------------

inline tm ST2TM(const SYSTEMTIME& st)
{
	tm t;
	memset(&t, 0, sizeof(t));
	if(IsZeroMemory(&st, sizeof(st)))
		return t;
	t.tm_year = st.wYear - 1900;
	t.tm_mon = st.wMonth - 1;
	t.tm_wday = st.wDayOfWeek;
	t.tm_mday = st.wDay;
	t.tm_hour = st.wHour;
	t.tm_min = st.wMinute;
	t.tm_sec = st.wSecond;
	t.tm_yday = 0;		// incomplete
	t.tm_isdst = -1;	// C run-time library code compute whether standard time or daylight saving time is in effect
	return t;
}

//------------------------------------------------------------------------

inline tm TIMET2TM(const time_t& tt)
{
	if(tt == 0)
	{
		struct tm t;
		memset(&t, 0, sizeof(t));
		return t;
	}

	struct tm t;
	memset(&t, 0, sizeof(t));
#if _MSC_VER <= 1200
	t = *gmtime(&tt);
#else
	gmtime_s(&t, &tt);			// time_t -> tm
#endif
	t.tm_isdst = -1; // C run-time library code compute whether standard time or daylight saving time is in effect
	return t;
}

//------------------------------------------------------------------------

inline time_t TM2TIMET(const tm& t)
{
	tm t2 = t;
	const time_t tt = _mkgmtime(&t2);	// tm -> time_t
	return tt == -1 ? 0 : tt;
}

//------------------------------------------------------------------------
//------------------------------------------------------------------------
//------------------------------------------------------------------------

inline SYSTEMTIME CurrentTimeAsST()
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));
	GetLocalTime(&st);
	return st;
}

//------------------------------------------------------------------------

inline SYSTEMTIME CurrentTimeAsST_GMT()
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));
	GetSystemTime(&st);
	return st;
}

//------------------------------------------------------------------------

inline FILETIME CurrentTimeAsFT()
{
	SYSTEMTIME st;
	memset(&st, 0, sizeof(st));
	GetLocalTime(&st);
	FILETIME ft;
	memset(&ft, 0, sizeof(ft));
	SystemTimeToFileTime(&st, &ft);
	return ft;
}

//------------------------------------------------------------------------

inline FILETIME CurrentTimeAsFT_GMT()
{
	FILETIME ft;
	memset(&ft, 0, sizeof(ft));
	GetSystemTimeAsFileTime (&ft);
	return ft;
}

//------------------------------------------------------------------------

inline time_t CurrentTimeAsTimeT_GMT() // Unix Time, seconds elapsed since midnight, January 1, 1970
{
	time_t unixtime = 0;
	time(&unixtime);		// get GMT
	return unixtime;
}

//------------------------------------------------------------------------

inline time_t CurrentTimeAsTimeT() // Unix Time, seconds elapsed since midnight, January 1, 1970
{
	const time_t unixtime = CurrentTimeAsTimeT_GMT();
#if _MSC_VER <= 1200
	struct tm *newtime = localtime(&unixtime);	// GMT time_t -> local tm
#else
	struct tm t;
	memset(&t, 0, sizeof(t));
	localtime_s(&t, &unixtime);	// GMT time_t -> local tm
	struct tm *newtime = &t;
#endif
	return TM2TIMET(*newtime);
}

//------------------------------------------------------------------------

inline tm CurrentTimeAsTm()
{
	const time_t unixtime = CurrentTimeAsTimeT_GMT();
#if _MSC_VER <= 1200
	struct tm *newtime = localtime(&unixtime);	// GMT time_t -> local tm
#else
	struct tm t;
	memset(&t, 0, sizeof(t));
	localtime_s(&t, &unixtime);	// GMT time_t -> local tm
	struct tm *newtime = &t;
#endif
	return *newtime;
}

//------------------------------------------------------------------------

inline tm CurrentTimeAsTm_GMT()
{
	const time_t unixtime = CurrentTimeAsTimeT_GMT();
	return TIMET2TM(unixtime);
}

//------------------------------------------------------------------------

class TimeZone
{
public:
	TimeZone();

	long	timezone;		// GMT+2 => -7200
	int		daylight;
	long	daylightoffset;	// usually -3600
};

extern TimeZone g_TimeZoneSet;

//------------------------------------------------------------------------

inline time_t GMT2Locale(const time_t& tt)
{
	if(tt == 0)
		return 0;
// 	return tt - g_TimeZoneSet.timezone - (g_TimeZoneSet.daylight ? g_TimeZoneSet.daylightoffset : 0);
//	return tt - g_TimeZoneSet.timezone;

#if _MSC_VER <= 1200
	struct tm *newtime = localtime(&tt);	// GMT time_t -> local tm
#else
	struct tm t;
	memset(&t, 0, sizeof(t));
	localtime_s(&t, &tt);	// GMT time_t -> local tm
	struct tm *newtime = &t;
#endif
	return TM2TIMET(*newtime);
}

//------------------------------------------------------------------------

inline time_t Locale2GMT(const time_t& tt)
{
	if(tt == 0)
		return 0;
// 	return tt + g_TimeZoneSet.timezone + (g_TimeZoneSet.daylight ? g_TimeZoneSet.daylightoffset : 0);
//	return tt + g_TimeZoneSet.timezone;

	tm t = TIMET2TM(tt);
	const time_t tt2 = mktime(&t);	// local tm -> GMT time_t
	return tt2;
}

//------------------------------------------------------------------------

inline SYSTEMTIME GMT2Locale(const SYSTEMTIME& st)
{
	SYSTEMTIME st2;
	memset(&st2, 0, sizeof(st2));
	if(IsZeroMemory(&st, sizeof(st)))
		return st2;
	SystemTimeToTzSpecificLocalTime(NULL, &st, &st2);
	return st2;
}

//------------------------------------------------------------------------

inline SYSTEMTIME Locale2GMT(const SYSTEMTIME& st)
{
	SYSTEMTIME st2;
	memset(&st2, 0, sizeof(st2));
	if(IsZeroMemory(&st, sizeof(st)))
		return st2;
	TzSpecificLocalTimeToSystemTime(NULL, &st, &st2);
	return st2;
}

//------------------------------------------------------------------------

inline std::string GetStringDate(const SYSTEMTIME& st = CurrentTimeAsST()) // 'YYYY-MM-DD' (for SQL)
{
	return StdString::Format("%04d-%02d-%02d", st.wYear, st.wMonth, st.wDay);
}

//------------------------------------------------------------------------

inline std::wstring GetStringDateW(const SYSTEMTIME& st = CurrentTimeAsST()) // 'YYYY-MM-DD' (for SQL)
{
	return StdString::FormatW(L"%04d-%02d-%02d", st.wYear, st.wMonth, st.wDay);
}

//------------------------------------------------------------------------

inline std::string GetStringTime(const SYSTEMTIME& st = CurrentTimeAsST()) // 'hh:mm:ss' (for SQL)
{
	return StdString::Format("%02d:%02d:%02d", st.wHour, st.wMinute, st.wSecond);
}

//------------------------------------------------------------------------

inline std::wstring GetStringTimeW(const SYSTEMTIME& st = CurrentTimeAsST()) // 'hh:mm:ss' (for SQL)
{
	return StdString::FormatW(L"%02d:%02d:%02d", st.wHour, st.wMinute, st.wSecond);
}

//------------------------------------------------------------------------

inline std::string GetStringDateTime(const SYSTEMTIME& st = CurrentTimeAsST()) // 'YYYY-MM-DD hh:mm:ss'
{
	return StdString::Format("%04d-%02d-%02d %02d:%02d:%02d",
		st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
}

//------------------------------------------------------------------------

inline std::wstring GetStringDateTimeW(const SYSTEMTIME& st = CurrentTimeAsST()) // 'YYYY-MM-DD hh:mm:ss'
{
	return StdString::FormatW(L"%04d-%02d-%02d %02d:%02d:%02d",
		st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
}

//------------------------------------------------------------------------

inline std::string GetStringDate(const int nDaysDeltaAgo, const FILETIME& ftNow = CurrentTimeAsFT())
{
	//	FILETIME ftNow = CurrentTimeAsFT();
	LARGE_INTEGER li = FT2LI(ftNow);

	li.QuadPart -= nDaysDeltaAgo * GetFileTimeDayTicks();

	const FILETIME ftMoved = LI2FT(li);
	SYSTEMTIME st;
	FileTimeToSystemTime(&ftMoved, &st);

	return GetStringDate(st);
}

//------------------------------------------------------------------------

int StringDateToDaysAgo(const std::string& sDate, const SYSTEMTIME& stNow = CurrentTimeAsST());

SYSTEMTIME ParseDbDateTime(const std::string& sDateTime);	// "YYYY-MM-DD HH:MM:SS"

//------------------------------------------------------------------------

inline std::string FormatTime(const std::string& sFmt = "%x %X", const tm& t = CurrentTimeAsTm())
{
	const int nMax = 200;
	char timeBuf[nMax] = "";
	const int nBytes = strftime(timeBuf, nMax-1, sFmt.c_str(), &t);
	if(nBytes == 0)
	{
		return "";
	}
	timeBuf[nMax-1] = 0;
	return timeBuf;	
}

//------------------------------------------------------------------------

bool IsLeapYear(const int nYear);

//------------------------------------------------------------------------

extern const LPCSTR g_szMonths[12];
extern const LPCSTR g_szWeekDays[7];

std::string FormatMonth(const int nMonth);	// "January" - 1, "February" - 2, "March" - 3, ...
std::string FormatShortMonth(const int nMonth);	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...
std::string FormatWeekday(const int nWeekday);	// "Sunday" = 0, "Monday" - 1, "Tuesday" - 2, ...
std::string FormatShortWeekday(const int nWeekday);	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...

//------------------------------------------------------------------------

int ParseShortWeekDay(const char ch1, const char ch2, const char ch3);	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...
int ParseShortMonthName(const char ch1, const char ch2, const char ch3);	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...

//------------------------------------------------------------------------
// ANSI C's asctime() format
// __TIMESTAMP__, The date and time of the last modification of the current source file.
// 'Ddd Mmm Date hh:mm:ss yyyy', where Ddd is the abbreviated day of the week and Date is an integer from 1 to 31
// Example: "Fri Sep  4 16:01:05 2009"

std::string FormatAnsiTimeStamp(const SYSTEMTIME& st);
tm ParseAnsiTimeStamp(const std::string& sTimeStamp);

//------------------------------------------------------------------------

//------------------------------------------------------------------------
// NOTE! See TimeHelpersGreta.h for RFC 822, RFC 850, RFC 2616 time formats!
//------------------------------------------------------------------------

//------------------------------------------------------------------------
// __DATE__, The compilation date of the current source file.
// 'Mmm dd yyyy'. The month name Mmm is the same as for dates generated by the library function asctime declared in TIME.H.
// Example: "Sep  4 2009"
// __TIME__, The most recent compilation time of the current source file.
// 'hh:mm:ss'
// Example: "16:01:05"

tm ParseDateAndTime(const std::string& sDate, const std::string& sTime);

//------------------------------------------------------------------------
// Some special functions for date/time parsing and formatting:

std::string FormatMonth(const int nMonth);	// "January" - 1, "February" - 2, "March" - 3, ...
std::string FormatShortMonth(const int nMonth);	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...
std::string FormatWeekday(const int nWeekday);	// "Sunday" = 0, "Monday" - 1, "Tuesday" - 2, ...
std::string FormatShortWeekday(const int nWeekday);	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...

int ParseShortWeekDay(const char ch1, const char ch2, const char ch3);	// 'Sun' - 0, 'Mon' - 1, 'Tue' - 2, ...
int ParseShortMonthName(const char ch1, const char ch2, const char ch3);	// 'Jan' - 1, 'Feb' - 2, 'Mar' - 3, ...
int ParseTimeZone(const std::string& sZone); // RFC 822, improved military zones, returns minutes offset from GMT

// Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
std::string FormatRFC822TimeStamp(const SYSTEMTIME& st);

// Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
// Sunday, 06-Nov-2005 08:49:37 GMT	- found
// Sunday, 6-Nov-2005 08:49:37 GMT	- found
std::string FormatRFC850TimeStamp(const SYSTEMTIME& st);

//------------------------------------------------------------------------

class ThreadAffinity1Chooser // choosing fixed processor for QueryPerformanceCounter function (see MSDN)
{
public:
	ThreadAffinity1Chooser()
	{
		m_dwThreadAffinityMask = ::SetThreadAffinityMask(::GetCurrentThread(), 1);
	}
	~ThreadAffinity1Chooser()
	{
		::SetThreadAffinityMask(::GetCurrentThread(), m_dwThreadAffinityMask);
	}
protected:
	DWORD_PTR	m_dwThreadAffinityMask;
};

//------------------------------------------------------------------------

template<class AFFINITY_CHOOSER>
class PTimer_T	// Performance Timer Template class
{
public:
	typedef __int64 int64;

	PTimer_T(): m_startTime(0), m_endTime(0), m_elapsedTime(0), m_frequency(0)
	{
		{
			AFFINITY_CHOOSER chooser; chooser; // "chooser;" - is to remove warning 'unreferenced local variable'
			::QueryPerformanceFrequency(reinterpret_cast< LARGE_INTEGER* >(&m_frequency));
		}
		Start();
	}

	inline void Start()
	{
		m_startTime = GetTime();
	}

	inline void Stop()
	{
		m_endTime = GetTime();
		m_elapsedTime += m_endTime - m_startTime;
	}

	inline int GetTimeInMs() const
	{
		return int((double(m_elapsedTime) / double(m_frequency)) * 1000.0f);
	}

	inline void Reset()
	{
		m_startTime = 0;
		m_endTime = 0;
		m_elapsedTime = 0;
	}

	inline int64 GetStart() const
	{
		return m_startTime;
	}

	inline int64 GetEnd() const
	{
		return m_endTime;
	}

	inline int64 GetElapsed() const
	{
		return m_elapsedTime;
	}

	inline int64 GetFrequency() const
	{
		return m_frequency;
	}

private:
	inline int64 GetTime() const
	{
		LARGE_INTEGER curTime;
		curTime.QuadPart = 0;
		{
			AFFINITY_CHOOSER chooser; chooser; // "chooser;" - is to remove warning 'unreferenced local variable'
			::QueryPerformanceCounter(&curTime);
		}
		return curTime.QuadPart;
	}

private:
	int64	m_startTime;
	int64	m_endTime;
	int64	m_elapsedTime;
	int64	m_frequency;
};

//------------------------------------------------------------------------

typedef PTimer_T<int>						PTimer;		// faster
typedef PTimer_T<ThreadAffinity1Chooser>	PTimer2;	// safer (using SetThreadAffinityMask)

//------------------------------------------------------------------------

#endif // !defined(AFX_TIMEHELPERS_H__5F43BC3F_FEC8_4718_88AD_455255DA8CCF__INCLUDED_)
