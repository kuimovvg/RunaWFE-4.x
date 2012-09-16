// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#if !defined(SAFECRITICALSECTION_H)
#define SAFECRITICALSECTION_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

//------------------------------------------------------

#ifdef COMPILE_MODULE_INFO
#include "timestamp.h"
#ifndef MODULE_TIME_STAMP_USED
#define MODULE_TIME_STAMP_USED
MODULE_TIME_STAMP
#endif
#endif //ifdef COMPILE_MODULE_INFO

//------------------------------------------------------
#if 0
class BaseCriticalSection
{
public:
	BaseCriticalSection(): m_pCS(NULL) {};

	inline void Lock() const {Init(); EnterCriticalSection(m_pCS);}
	inline bool TryLock() const {Init(); return !!TryEnterCriticalSection(m_pCS);}
	inline void Unlock() const {Init(); LeaveCriticalSection(m_pCS);}

	inline void Init() const
	{
		if(!m_pCS)
		{
			m_pCS = new CRITICAL_SECTION;
			InitializeCriticalSection(m_pCS);
		}
	}
	inline void Delete() const
	{
		if(m_pCS)
		{
			DeleteCriticalSection(m_pCS);
			delete m_pCS;
			m_pCS = NULL;
		}
	}

	inline void MarkUninitialized() const
	{
		m_pCS = NULL;
	}

protected:
	mutable	CRITICAL_SECTION*	m_pCS;
};

//------------------------------------------------------

typedef BaseCriticalSection GlobalCriticalSection;

//------------------------------------------------------

class SafeCriticalSection: public BaseCriticalSection
{
public:
	inline SafeCriticalSection() {MarkUninitialized(); Init();}
	inline SafeCriticalSection(const SafeCriticalSection& b) {MarkUninitialized(); Init();} // DON'T copy CRITICAL_SECTION!

	inline ~SafeCriticalSection() {Delete();}

	inline SafeCriticalSection& operator =(const SafeCriticalSection& b) {return *this;} // DON'T copy CRITICAL_SECTION!
};

//------------------------------------------------------
#else
class SafeCriticalSection
{
public:
	inline SafeCriticalSection() {InitializeCriticalSection(&m_sec);}
	inline SafeCriticalSection(const SafeCriticalSection& b) {InitializeCriticalSection(&m_sec);} // DON'T copy CRITICAL_SECTION!

	inline ~SafeCriticalSection() {DeleteCriticalSection(&m_sec);}

	inline SafeCriticalSection& operator =(const SafeCriticalSection& b) {return *this;} // DON'T copy CRITICAL_SECTION!

public:
	inline void Lock() const {EnterCriticalSection(&m_sec);}
	inline bool TryLock() const {return !!TryEnterCriticalSection(&m_sec);}
	inline void Unlock() const {LeaveCriticalSection(&m_sec);}

	inline void Init() const {};	// unused
	inline void Delete() const {};	// unused
	inline void MarkUninitialized() const {};	// unused

protected:
	mutable	CRITICAL_SECTION m_sec;
};

typedef SafeCriticalSection GlobalCriticalSection;
typedef SafeCriticalSection BaseCriticalSection;

#endif
//------------------------------------------------------

class SafeCSLock
{
public:
	inline SafeCSLock(const BaseCriticalSection& b, const bool bSkipInitialLock = false): m_CriticalSection(b)
	{
		if(!bSkipInitialLock)
		{
			m_CriticalSection.Lock();
		}
	}

	inline ~SafeCSLock()
	{
		m_CriticalSection.Unlock();
	}

protected:
	const BaseCriticalSection&	m_CriticalSection;
};

//------------------------------------------------------
#if 0
class SafeCSLock
{
public:
	inline SafeCSLock(const SafeCriticalSection& b, const bool bSkipInitialLock = false): m_SafeCriticalSection(b)
	{
		if(!bSkipInitialLock)
		{
			m_SafeCriticalSection.Lock();
		}
	}

	inline ~SafeCSLock()
	{
		m_SafeCriticalSection.Unlock();
	}

protected:
	const SafeCriticalSection&	m_SafeCriticalSection;
};
#endif
//------------------------------------------------------

template<class T>
class Singleton
{
public:
	inline Singleton(): m_pObject(NULL) { Init(); };
	inline ~Singleton() { Uninit(); };

	inline T& get()
	{
		{
			SafeCSLock lock(m_cs);
			if(!m_pObject)
				m_pObject = new T;
		}
		return *m_pObject;
	};

	void clear()
	{
		Delete();
	}

protected:
	inline Singleton(const Singleton& b): m_pObject(NULL) {}; // do nothing
	inline Singleton& operator =(const Singleton& b) {return *this}; // do nothing

	inline void Init()
	{
		m_cs.MarkUninitialized();
		m_cs.Init();
	}

	inline void Delete()
	{
		if(m_pObject)
		{
			delete m_pObject;
			m_pObject = NULL;
		}
	}

	inline void Uninit()
	{
		Delete();
		m_cs.Delete();
		m_cs.MarkUninitialized();
	}

	bool					m_bInitialized;
	GlobalCriticalSection	m_cs;
	T*						m_pObject;
};

//------------------------------------------------------

#endif // !defined(SAFECRITICALSECTION_H)
