// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#if !defined(AFX_FILEHELPERS_H__4A7A47C6_72B4_40E3_B9AB_2D44B9F88B8E__INCLUDED_)
#define AFX_FILEHELPERS_H__4A7A47C6_72B4_40E3_B9AB_2D44B9F88B8E__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <string>
#include <vector>

#include <stdio.h>
#include <stdarg.h>

#include <io.h>
//#include <fcntl.h>

#include "basedef.h"
#include "SafeCriticalSection.h"

#ifdef UNITTEST_DEPENDENCY
#include "Modules.h"
LINK_TO_MODULE(FileHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

FILE* my_fopen_aplus(const tstring& filename, const tstring& mode); // mode should include "a+"!!!
FILE* fopen_my(const std::string& sFileName, const std::string& sAccess);

//------------------------------------------------------

#define BOM_UTF8					"\xEF\xBB\xBF"
#define BOM_UNICODE_LITTLE_ENDIAN	"\xFF\xFE"		// Intel
#define BOM_UNICODE_BIG_ENDIAN		"\xFE\xFF"
#define BOM_UNICODE_WORD			wchar_t(0xFEFF)

//------------------------------------------------------

bool GetFiles(const tstring& sFileMask, std::vector<tstring>& vectFiles);
bool GetFiles(const tstring& sFileMask, std::vector<tstring>& vectFiles, std::vector<tstring>& vectFolders);

void GetFilesRecursive(const tstring& sFileMask, std::vector<tstring>& vectFiles);

tstring MyGetLongPathName(const tstring& sPathName);	// convert a possibly 8.3 file name into it's full form

tstring GetFileFolder(const tstring& sFileName); // without '/' at the end
tstring GetFileName(const tstring& sFileFullName); // Gets only file name (C:\folder\file.txt => file.txt)
tstring GetFileExtension(const tstring& sFileName); // Gets only file extension (C:\folder\file.txt => txt)
tstring TrimFileExtension(const tstring& sFileName); // Gets full file name without extension (C:\folder\file.txt => C:\folder\file)

DWORD GetFileSize32(const tstring& sFileName);	// INVALID_FILE_SIZE on error
INT64 GetFileSize64(const tstring& sFileName);	// -1 on error

bool myCreateDirectory(const tstring& sFullPath, HWND hwnd = NULL); // and creates all its parents

bool myRemoveDirectory(const tstring& sFullPath, HWND hwnd = NULL); // it may be non-empty

bool LoadFileToVector(const tstring& sFileName, std::vector<BYTE>& vectData);
bool SaveFileFromVector(const tstring& sFileName, const std::vector<BYTE>& vectData);

bool LoadFileToString(const tstring& sFileName, std::string& sBinaryData);			// read any binary data
bool SaveFileFromString(const tstring& sFileName, const std::string& sBinaryData);	// writes any binary data

bool LoadTextFileToString(const tstring& sFileName, std::wstring& strString); // skips BOM at the beginning of the file, read WORD-aligned binary data
bool SaveTextFileFromString(const tstring& sFileName, const std::wstring& strString);	// writes WORD-aligned binary data

bool LoadTextFileToString(const tstring& sFileName, std::string& strString);	// skips UTF BOM at the beginning of the file
bool SaveTextFileFromString(const tstring& sFileName, const std::string& strString); // Same as SaveFileFromString

tstring GetExeFullName(void); // 'c:\folder\program.exe'
tstring GetExeName(void); // 'c:\folder\program.exe' -> 'program'
tstring GetExeFolder(void); // With trailing '\'
tstring MyExpandEnvironmentStrings(const tstring& sFileName);

tstring GetExeRelativePath(const tstring& sRelativePath); // makes relative path absolute
tstring FormatSamplesFolderRelativePath(const tstring& path);	// i.e. "Documentation" folder

tstring GetWindowsFolder(void);

bool IsFileExists(const tstring& sFileName);
bool IsFolderExists(const tstring& sFileName);

void SaveToLog(const std::string& sLogRecord, const tstring& sExeRelativePath);

//------------------------------------------------------

class Config
{
public:
	Config(const tstring& sIniFile = _T("")); // using sIniFile == "" for main program's INI
	void Initialize(const tstring& sIniFile = _T("")); // using sIniFile == "" for main program's INI

	inline void Initialize(const Config& b)
	{
		SafeCSLock lock(m_protect);
		Initialize(b.GetIniFileName());
	}

	void DeleteValue(const tstring& sSection, const tstring& sKey);

	void SetString(const tstring& sSection, const tstring& sKey, const tstring& sValue);
	void SetInt(const tstring& sSection, const tstring& sKey, const int nValue);
	std::string GetStringA(const tstring& sSection, const tstring& sKey, const std::string& sDefaultValue = "") const; // this is useful for UTF8
	tstring GetString(const tstring& sSection, const tstring& sKey, const tstring& sDefaultValue = _T("")) const;
	int GetInt(const tstring& sSection, const tstring& sKey, const int nDefaultValue = 0) const;

	void GetStringArray(const tstring& sSection, const tstring& sKey, std::vector<tstring>& vect, const size_t nStartIndex = 0) const;
	void SetStringArray(const tstring& sSection, const tstring& sKey, const std::vector<tstring>& vect, const size_t nStartIndex = 0);

	tstring GetIniFileName() const;
public:

	tstring						m_sIniFileName;
	static SafeCriticalSection	m_protect;	// to ensure multiple threads will call object methods one after another
};

bool GetIniSectionList(const tstring& sIniFile, std::vector<tstring>& vectSections);

//------------------------------------------------------

class CMyFile
{
public:
	inline CMyFile()
	{
		m_pFile = NULL;
	}

	inline ~CMyFile()
	{
		close();
	}

	inline operator HANDLE() const
	{
		return (HANDLE)_get_osfhandle(_fileno(m_pFile));
	}

	inline void close()
	{
		if(m_pFile != NULL)
		{
			fclose(m_pFile);
			m_pFile = NULL;
		}
	}

	inline bool open(const std::string& sFileName, const std::string& access)
	{
		close();
		m_pFile = fopen_my(sFileName.c_str(), access.c_str());
		return m_pFile != NULL;
	}

	inline bool IsOpened() const
	{
		return m_pFile != NULL;
	}

	inline long GetLength() const
	{
		if(m_pFile == NULL)
			return 0;

		const long old_pos = ftell(m_pFile);
		fseek(m_pFile, 0, SEEK_END);
		const int file_size = ftell(m_pFile);
		fseek(m_pFile, old_pos, SEEK_SET);
		return file_size;
	}

	inline void seek(const long offset, const int whence = SEEK_SET) const
	{
		if(m_pFile != NULL)
		{
			fseek(m_pFile, offset, whence);
		}
	}

	inline size_t read(void* pBuffer, const int nBytes) const
	{
		size_t res = 0;
		if(m_pFile != NULL)
		{
			res = fread(pBuffer, 1, nBytes, m_pFile);
		}
		return res;
	}

	inline size_t write(const void* pBuffer, const int nBytes)
	{
		size_t res = 0;
		if(m_pFile != NULL)
		{
			res = fwrite(pBuffer, 1, nBytes, m_pFile);
		}
		return res;
	}

	inline int printf(const char *format, ...)
	{
		int written = 0;
		va_list vl;
		va_start(vl, format);
		if(m_pFile != NULL)
		{
			written = vfprintf(m_pFile, format, vl);
		}
		va_end(vl);
		return written;
	}

	//----------------------------

	void AppendTextWithOpeningFile(const tstring& sFileName, const std::string& sAppendText); // VERY SPECIAL MEMBER!
	void AppendTextWithOpeningFile(const tstring& sFileName, const std::wstring& sAppendText); // VERY SPECIAL MEMBER!

	//----------------------------

protected:
	FILE*				m_pFile;
	SafeCriticalSection m_protectFile;
};

//------------------------------------------------------

#endif // !defined(AFX_FILEHELPERS_H__4A7A47C6_72B4_40E3_B9AB_2D44B9F88B8E__INCLUDED_)
