// SmartProfiler.h: interface for the SmartProfiler class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_SMARTPROFILER_H__65E0B89D_81B0_49CF_912A_3DCC307238BE__INCLUDED_)
#define AFX_SMARTPROFILER_H__65E0B89D_81B0_49CF_912A_3DCC307238BE__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <string>

//#include "..\encode\crypt_macro2.h"

// =====================================
// STUBS:
// =====================================

#ifdef UNITTEST_DEPENDENCY
#include "..\Modules.h"
LINK_TO_MODULE(SmartProfiler)
#endif

//------------------------------------------------------------------------

//#define OMIT_PROFILER
//#define ENCODE_PROFILER_INFO

//------------------------------------------------------------------------

#define Thread   __declspec( thread )
/*
class TlsValue
{
public:

	inline TlsValue()
	{
		m_dwIndex = TlsAlloc();
	}

	inline ~TlsValue()
	{
		if(m_dwIndex != TLS_OUT_OF_INDEXES)
		{
			TlsFree(m_dwIndex);
			m_dwIndex = TLS_OUT_OF_INDEXES;
		}
	}

	inline DWORD GetTlsIndex() const
	{
		return m_dwIndex;
	}

protected:

	DWORD	m_dwIndex;
};
*/

//------------------------------------------------------------------------

void* GetEip();	// GetCurrentAddress()

//------------------------------------------------------------------------

#define PROFILER_BAD_PTR_VALUE	((ProfilerEpisode*)1982) // 0x7BE
#define PROFILER_MAGIC1	0x2354A873
#define PROFILER_MAGIC2	0x983AF842

class ProfilerEpisode
{
	friend class SmartProfiler;
public:
	ProfilerEpisode(LPCSTR pszName, LPCSTR szFileName, const int nLine, void* pCodeAddress)
	{
		const DWORD le = GetLastError();
		m_dwMagic1 = PROFILER_MAGIC1;
		m_dwMagic2 = PROFILER_MAGIC2;
		m_pszName = pszName;
#ifdef ENCODE_PROFILER_INFO
		m_sName = DecodeCrypt2String(pszName);
#else
		m_sName = m_pszName;
#endif
		m_nChildTicks = 0;
//		m_pParent = m_pCurrentEpisode;	m_pCurrentEpisode = this; // add episode to stack
//		m_dwOriginalTlsIndex = m_CurrentEpisodeTlsIndex.GetTlsIndex();
		m_pParent = GetCurrentThreadEpisode();
		SetCurrentThreadEpisode(this); // add episode to stack
		m_pszFile = szFileName;
		m_nLine = nLine;
		m_sThread = FormatCurrentThreadName();
		m_pCodeAddress = pCodeAddress;
		if(m_sName.empty())
			m_sName = FormatDefaultName();
		QueryPerformanceCounter(&m_liStart);
		InsideConstructor();
		SetLastError(le);
	}

	~ProfilerEpisode()
	{
		const DWORD le = GetLastError();
		LARGE_INTEGER liEnd;
		QueryPerformanceCounter(&liEnd);

		SaveEpisode(liEnd.QuadPart - m_liStart.QuadPart);
		//m_pParent = NULL; // just for any case
		m_pParent = PROFILER_BAD_PTR_VALUE; // just for any case
		SetLastError(le);
	}

	static std::string GetProfileEpisodeFullName(
		IN const ProfilerEpisode* pEpisode,
		IN const bool bGoThoughRecursions,
		OUT bool& bFoundAnyParents,
		OUT bool& bFoundSameEpisode);

	static inline ProfilerEpisode* GetCurrentThreadEpisode()
	{
		//return (ProfilerEpisode*)TlsGetValue(m_CurrentEpisodeTlsIndex.GetTlsIndex());
		return m_pCurrentEpisode;
	}

	static inline void SetCurrentThreadEpisode(ProfilerEpisode* ptr)
	{
		//TlsSetValue(m_CurrentEpisodeTlsIndex.GetTlsIndex(), ptr);
		m_pCurrentEpisode = ptr;
	}

public:
	std::string GetFileLine() const;

	inline std::string GetName() const
	{
		return m_sName;
	}

	inline std::string GetThreadName() const
	{
		return m_sThread;
	}

	inline void* GetCodeAddress() const
	{
		return m_pCodeAddress;
	}

	inline void SetCodeAddress(void* p)
	{
		m_pCodeAddress = p;
	}

protected:
	std::string FormatDefaultName() const;
	std::string FormatCurrentThreadName();
	void SaveEpisode(const INT64 nTicks);
	void InsideConstructor();

	DWORD				m_dwMagic1;
	LPCSTR				m_pszName;
	std::string			m_sName;
	LARGE_INTEGER		m_liStart;
	INT64				m_nChildTicks;
	ProfilerEpisode*	m_pParent;
	LPCSTR				m_pszFile;
	int					m_nLine;
	std::string			m_sThread;
	//DWORD				m_dwOriginalTlsIndex;

	void*				m_pCodeAddress;

	DWORD				m_dwMagic2;

	//static TlsValue	m_CurrentEpisodeTlsIndex;
	Thread static ProfilerEpisode*	m_pCurrentEpisode; // no protection is needed for thread data
};

//------------------------------------------------------------------------

inline std::string GetCurrentCallStack()
{
	bool bFoundAnyParents = false;
	bool bFoundSameEpisode = false;
	return ProfilerEpisode::GetProfileEpisodeFullName(ProfilerEpisode::GetCurrentThreadEpisode(),
		true, bFoundAnyParents, bFoundSameEpisode);
}

//------------------------------------------------------------------------

void AppendFunctionName(void* pAddress, const std::string& sName);
std::string SearchFunctionName(void* pFunctionRealAddress, const int nMaxDistance, int& nDistance);
int GetProfilerNameCount();
std::string DumpAllProfilerNames();
void DeleteAllFunctionNameItems();
void DeleteAllProfilerItems();

//------------------------------------------------------------------------

#ifndef OMIT_PROFILER

#ifdef ENCODE_PROFILER_INFO
#define PROFILER		ProfilerEpisode episodeFunction(STR_CRYPT2(__FUNCTION__), STR_CRYPT2(__FILE__), __LINE__, GetEip())
#define PROFILE(name)	ProfilerEpisode episode(STR_CRYPT2(name), STR_CRYPT2(__FILE__), __LINE__, GetEip())
#define PROFILE2(name)	ProfilerEpisode episode2(STR_CRYPT2(name), STR_CRYPT2(__FILE__), __LINE__, GetEip())
#define PROFILE3(name)	ProfilerEpisode episode3(STR_CRYPT2(name), STR_CRYPT2(__FILE__), __LINE__, GetEip())
#define PROFILE4(name)	ProfilerEpisode episode4(STR_CRYPT2(name), STR_CRYPT2(__FILE__), __LINE__, GetEip())
#else
#define PROFILER		ProfilerEpisode episodeFunction(__FUNCTION__, __FILE__, __LINE__, GetEip())
#define PROFILE(name)	ProfilerEpisode episode(name, __FILE__, __LINE__, GetEip())
#define PROFILE2(name)	ProfilerEpisode episode2(name, __FILE__, __LINE__, GetEip())
#define PROFILE3(name)	ProfilerEpisode episode3(name, __FILE__, __LINE__, GetEip())
#define PROFILE4(name)	ProfilerEpisode episode4(name, __FILE__, __LINE__, GetEip())
#endif

void PrintProfileReport();

#else

#define PROFILER
#define PROFILE(name)
#define PROFILE2(name)
#define PROFILE3(name)
#define PROFILE4(name)

inline void PrintProfileReport()
{
}

#endif

//------------------------------------------------------------------------

#endif // !defined(AFX_SMARTPROFILER_H__65E0B89D_81B0_49CF_912A_3DCC307238BE__INCLUDED_)
