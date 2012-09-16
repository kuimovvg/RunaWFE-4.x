// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#include "stdafx.h"
#include "FileHelpers.h"
#include "SafeCriticalSection.h"
#include "TextHelpers.h"

#include <atlbase.h>
#include <atlconv.h>

#include <Shlwapi.h>	// for PathIsRelative, PathRemoveFileSpec, PathFindFileName, etc
#pragma comment(lib, "shlwapi.lib")

#include <shellapi.h>		// for SHFileOperation
#include <shlobj.h>		// for SHCreateDirectory
#pragma comment(lib, "shell32.lib")

#include "Tracing.h"

#include <io.h>

#ifdef UNITTEST_DEPENDENCY
#include "UnitTest\UnitTest.h"
IMPLEMENT_MODULE(FileHelpers)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

FILE* my_fopen_aplus(const tstring& filename, const tstring& mode) // mode should include "a+"!!!
{
#if _MSC_VER <= 1200
	return _tfopen(filename.c_str(), mode.c_str());
#else
// 	FILE* f = NULL;
// 	errno_t err = _tfopen_s(&f, filename.c_str(), mode.c_str());
// 	if(err != 0)
// 		f = NULL;
// 	return f;
	return _tfsopen(filename.c_str(), mode.c_str(), _SH_DENYNO);
#endif
/*
	int fd = 0;
	if( (fd = _open(filename, _O_WRONLY | _O_APPEND | _O_CREAT)) == -1 )
	{
		printf("_open(\"%s\") failed!\n", filename);
		return NULL;
	}

	FILE* stream = NULL;
	// Get stream from file descriptor.
	if( (stream = _fdopen( fd, mode )) == NULL )
	{
		printf("_fdopen_open(\"%s\", \"%s\") failed!\n", filename, mode);
		_close( fd );
		return NULL;
	}

	return stream;
*/
}

//------------------------------------------------------

FILE* fopen_my(const std::string& sFileName, const std::string& sAccess)
{
	FILE* f = NULL;
#if _MSC_VER <= 1200
	f = fopen(sFileName.c_str(), sAccess.c_str());
#else
	// 	errno_t err = fopen_s(&f, sFileName.c_str(), sAccess.c_str());
	// 	if(err != 0)
	// 	{
	// 		f = NULL;
	// 	}
	f = _fsopen(sFileName.c_str(), sAccess.c_str(), _SH_DENYNO);
#endif
	return f;
}

//------------------------------------------------------

bool GetFiles(const tstring& sFileMask, std::vector<tstring>& vectFiles, std::vector<tstring>& vectFolders)
{
/*
	CFileFind finder;
	vectFiles.clear();
	BOOL bWorking = finder.FindFile(sFileMask.c_str());
	while (bWorking)
	{
		bWorking = finder.FindNextFile();

		if (finder.IsDots())
			continue;

		CString sFilePath = finder.GetFilePath();
		if (!finder.IsDirectory())
		{
			vectFiles.push_back((LPCSTR)sFilePath);
		}
	}
	return true;
*/

	vectFiles.clear();
	vectFolders.clear();
	_tfinddata_t c_file = {0};
	long hFile = 0;

	const tstring sFolder = GetFileFolder(sFileMask);

	hFile = _tfindfirst(sFileMask.c_str(), &c_file);
	if ( hFile == -1L )
	{
		if(errno == ENOENT)
		{
			//printf("No files found!\n");
		}
		else
		{
			MYTRACE("ERROR! GetFiles failed: errno = %d\n", errno);
			return false;
		}
	}
	else
	{
		do
		{
			//printf("%s\n", c_file.name);
			const tstring& sFileName = c_file.name;
			if(sFileName == _T(".") || sFileName == _T(".."))
			{
				continue;
			}

			const tstring& sFullFileName = sFolder + _T("\\") + c_file.name;
			if(IsFolderExists(sFullFileName))
			{
				vectFolders.push_back(sFullFileName);
				continue;
			}

			vectFiles.push_back(sFullFileName);
		} while ( _tfindnext(hFile, &c_file) == 0 );
		_findclose(hFile);
	}
	return true;
}

//------------------------------------------------------

bool GetFiles(const tstring& sFileMask, std::vector<tstring>& vectFiles)
{
	std::vector<tstring> vectFolders;
	return GetFiles(sFileMask, vectFiles, vectFolders);
}

//------------------------------------------------------

void GetFilesRecursive(const tstring& sFileMask, std::vector<tstring>& vectFiles)
{
	const tstring sFolder = GetFileFolder(sFileMask);
	const tstring sMask = GetFileName(sFileMask);

	std::vector<tstring> vectFiles2;
	std::vector<tstring> vectFolders2;
	GetFiles(sFileMask, vectFiles2, vectFolders2);

	if(sMask != _T("*.*"))
	{
		std::vector<tstring> vectFiles3;
		GetFiles(sFolder + _T("\\*.*"), vectFiles3, vectFolders2);
	}

	AppendVector(vectFiles, vectFiles2);
	vectFiles2.clear();

	for(size_t i = 0; i < vectFolders2.size(); i++)
	{
		GetFilesRecursive(vectFolders2[i] + _T("\\") + sMask, vectFiles);
	}
}

//------------------------------------------------------

tstring MyGetLongPathName(const tstring& sPathName)	// convert a possibly 8.3 file name into it's full form
{
	TCHAR szPath[MAX_PATH] = _T("");
	if(sPathName.empty())
		return _T("");
	const DWORD dwRet = GetLongPathName(sPathName.c_str(), szPath, sizeof(szPath)/sizeof(szPath[0]));
	if(dwRet == 0 || dwRet >= sizeof(szPath)/sizeof(szPath[0]))
		return _T("");
	return tstring(szPath, dwRet);
}

//------------------------------------------------------

tstring GetFileFolder(const tstring& sFileName)
{
	TCHAR szPath[MAX_PATH];
#if _MSC_VER <= 1200
	_tcsncpy(szPath, sFileName.c_str(), MAX_PATH);
#else
	_tcsncpy_s(szPath, sFileName.c_str(), MAX_PATH);
#endif
	PathRemoveFileSpec(szPath);
	return szPath;
}

//------------------------------------------------------

tstring GetFileName(const tstring& sFileFullName) // Gets only file name (C:\folder\file.txt => file.txt)
{
	LPCTSTR psz = PathFindFileName(sFileFullName.c_str());
	return psz;
}

//------------------------------------------------------

tstring GetFileExtension(const tstring& sFileName) // Gets only file extension (C:\folder\file.txt => txt)
{
	LPCTSTR psz = PathFindExtension(sFileName.c_str());
	if(NULL == psz)
	{
		// this should never happen!
		return _T("");
	}
	if(_T('.') == *psz)
	{
		return psz+1;
	}
	return psz; // pointer to last terminating null-character
}

//------------------------------------------------------

tstring TrimFileExtension(const tstring& sFileName) // Gets full file name without extension (C:\folder\file.txt => C:\folder\file)
{
	LPCTSTR psz0 = sFileName.c_str();
	LPCTSTR psz = PathFindExtension(psz0);
	if(NULL == psz)
	{
		// this should never happen!
		return sFileName;
	}
	if(_T('.') == *psz)
	{
		return sFileName.substr(0, psz - psz0);
	}
	return sFileName; // pointer to last terminating null-character
}

//------------------------------------------------------

bool myCreateDirectory(const tstring& sFullPath, HWND hwnd) // and creates all its parents
{
	TCHAR szPath[MAX_PATH] = _T("");
	DWORD dwRes = GetFullPathName(sFullPath.c_str(), MAX_PATH, szPath, NULL);
	if(dwRes == 0 || dwRes > MAX_PATH)
	{
		MYASSERT(FALSE && "This should never happen!");
		return false;
	}
	if(IsFolderExists(szPath))
	{
		return true;
	}
	const int res = SHCreateDirectoryEx(hwnd, szPath, NULL);
	return ERROR_SUCCESS == res;
}

//------------------------------------------------------

bool myRemoveDirectory(const tstring& sFullPath, HWND hwnd) // it may be non-empty
{
	if(!IsFolderExists(sFullPath))
	{
		return true;
	}

	SHFILEOPSTRUCT shfOper;
	ZeroMemory(&shfOper, sizeof(shfOper));
	TCHAR szTemp[MAX_PATH];
	// Delete Directory
	shfOper.hwnd = hwnd;
	shfOper.wFunc = FO_DELETE;

#if _MSC_VER <= 1200
	_tcsncpy(szTemp, sFullPath.c_str(), MAX_PATH);
#else
	_tcsncpy_s(szTemp, sFullPath.c_str(), MAX_PATH);
#endif
	szTemp[_tcslen(szTemp)+1] = 0;// double null-terminated
	shfOper.pFrom = szTemp;
	shfOper.fFlags = FOF_NOCONFIRMATION;

	const int nRet = SHFileOperation(&shfOper);
	return !nRet; // 0 is GOOD!
}

//------------------------------------------------------

bool LoadFileToVector(const tstring& sFileName, std::vector<BYTE>& vectData)
{
	vectData.clear();
	HANDLE hFile = CreateFile( sFileName.c_str(), GENERIC_READ,
		FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
	if( hFile == INVALID_HANDLE_VALUE )
		return false;

	const DWORD size = GetFileSize( hFile, NULL );

	vectData.resize(size);
	if(!vectData.empty())
	{
		PBYTE pBuffer = &vectData[0];

		DWORD read_bytes = 0;
		const bool bRes = !!ReadFile( hFile, pBuffer, size, &read_bytes, NULL );
		MYASSERT(bRes && "ReadFile failed!");
		MYASSERT( read_bytes == size );
	}

	CloseHandle( hFile );
	return true;
}

//------------------------------------------------------

INT64 GetFileSize64(const tstring& sFileName)	// -1 on error
{
	HANDLE hFile = CreateFile( sFileName.c_str(), FILE_READ_ATTRIBUTES,
		FILE_SHARE_READ|FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);
	if( hFile == INVALID_HANDLE_VALUE )
		return -1;

	LARGE_INTEGER li;
	li.QuadPart = 0;
	const bool bRes = !!GetFileSizeEx(hFile, &li);
	if(!bRes)
	{
		CloseHandle(hFile);
		return -1;
	}

	CloseHandle(hFile);
	return li.QuadPart;
}

//------------------------------------------------------

DWORD GetFileSize32(const tstring& sFileName)	// -1 on error
{
	HANDLE hFile = CreateFile( sFileName.c_str(), FILE_READ_ATTRIBUTES,
		FILE_SHARE_READ|FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);
	if( hFile == INVALID_HANDLE_VALUE )
		return INVALID_FILE_SIZE;

	const DWORD dwSize = GetFileSize( hFile, NULL );
	if(dwSize == INVALID_FILE_SIZE)
	{
		CloseHandle(hFile);
		return INVALID_FILE_SIZE;
	}

	CloseHandle(hFile);
	return dwSize;
}

//------------------------------------------------------

static inline bool PrepareBeforeCreatingFile(const tstring& sFileName)
{
	BOOL res = FALSE;

	tstring sFolder = GetFileFolder(sFileName);
	if(!sFolder.empty())
	{
		res = myCreateDirectory(sFolder);
		if(!res)
		{
			return false;
		}
	}

	const DWORD attr = GetFileAttributes(sFileName.c_str());
	if(attr != INVALID_FILE_ATTRIBUTES)
	{
		if((attr&FILE_ATTRIBUTE_READONLY) != 0)
		{
			SetFileAttributes(sFileName.c_str(), (attr&(~FILE_ATTRIBUTE_READONLY)));
		}
	}
	return true;
}

//------------------------------------------------------

static inline BOOL SaveBinaryFile(const tstring& sFileName, LPCVOID pData, int nBytes)
{
	BOOL res;
	const DWORD attr = GetFileAttributes(sFileName.c_str());
	if(attr != INVALID_FILE_ATTRIBUTES)
	{
		if((attr&FILE_ATTRIBUTE_READONLY) != 0)
		{
			SetFileAttributes(sFileName.c_str(), (attr&(~FILE_ATTRIBUTE_READONLY)));
		}
	}

	const HANDLE hFile = CreateFile(sFileName.c_str(), GENERIC_WRITE, FILE_SHARE_READ,
		NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	res = hFile != INVALID_HANDLE_VALUE;
	if(!res)
		return res;

	DWORD written = 0;
	WriteFile(hFile, pData, nBytes, &written, NULL);

	CloseHandle(hFile);
	return TRUE;
}

//------------------------------------------------------

bool SaveFileFromVector(const tstring& sFileName, const std::vector<BYTE>& vectData)
{
	if(!PrepareBeforeCreatingFile(sFileName))
		return false;

	if(vectData.empty())
	{
		return !!SaveBinaryFile(sFileName, "", 0);
	}
	return !!SaveBinaryFile(sFileName, &vectData[0], vectData.size());
}

//------------------------------------------------------

bool LoadFileToString(const tstring& sFileName, std::string& sBinaryData)			// read any binary data
{
	sBinaryData.clear();
	std::vector<BYTE> vect;
	const bool bRes = LoadFileToVector(sFileName, vect);
	if(!bRes)
		return false;

	if(vect.empty())
		return true;
	sBinaryData = std::string((LPCSTR)&vect[0], vect.size());
	return true;
}

//------------------------------------------------------

bool SaveFileFromString(const tstring& sFileName, const std::string& sBinaryData)	// writes any binary data
{
	if(!PrepareBeforeCreatingFile(sFileName))
		return false;

	return !!SaveBinaryFile(sFileName, sBinaryData.data(), sBinaryData.size());
}

//------------------------------------------------------

bool LoadTextFileToString(const tstring& sFileName, std::wstring& strString) // skips BOM at the beginning of the file, read WORD-aligned binary data
{
	strString.clear();
	std::vector<BYTE> vect;
	const bool bRes = LoadFileToVector(sFileName, vect);
	if(!bRes)
		return false;

	if(vect.empty())
		return true;

	const size_t nBomSize = sizeof(BOM_UNICODE_LITTLE_ENDIAN)-1; // 2
	if(vect.size() >= nBomSize)
	{
		if(vect[0] == BYTE(BOM_UNICODE_LITTLE_ENDIAN[0]) && vect[1] == BYTE(BOM_UNICODE_LITTLE_ENDIAN[1]))
		{
			//strString = std::wstring((LPCWSTR)&vect[nBomSize], (vect.size()-nBomSize)/sizeof(wchar_t));
			const size_t nNewSize = (vect.size()-nBomSize)/sizeof(wchar_t);
			strString.resize(nNewSize);
			for(size_t i = 0; i < nNewSize; i++)
			{
				const size_t ind = nBomSize + i*sizeof(wchar_t);
				strString[i] = (wchar_t(vect[ind])) | (wchar_t(vect[ind+1])<<8);
			}
			return true;
		}
		if(vect[0] == BYTE(BOM_UNICODE_BIG_ENDIAN[0]) && vect[1] == BYTE(BOM_UNICODE_BIG_ENDIAN[1]))
		{
			//strString = std::wstring((LPCWSTR)&vect[nBomSize], (vect.size()-nBomSize)/sizeof(wchar_t));
			const size_t nNewSize = (vect.size()-nBomSize)/sizeof(wchar_t);
			strString.resize(nNewSize);
			for(size_t i = 0; i < nNewSize; i++)
			{
				const size_t ind = nBomSize + i*sizeof(wchar_t);
				strString[i] = (wchar_t(vect[ind])<<8) | (wchar_t(vect[ind+1]));
			}
			return true;
		}
	}
	strString = std::wstring((LPCWSTR)&vect[0], vect.size()/sizeof(wchar_t));
	return true;
}

//------------------------------------------------------

bool SaveTextFileFromString(const tstring& sFileName, const std::wstring& strString)	// writes WORD-aligned binary data
{
	if(!PrepareBeforeCreatingFile(sFileName))
		return false;

	return !!SaveBinaryFile(sFileName, strString.data(), strString.size()*sizeof(wchar_t));
}

//------------------------------------------------------

bool LoadTextFileToString(const tstring& sFileName, std::string& strString)
{
	strString.clear();
	std::vector<BYTE> vect;
	const bool bRes = LoadFileToVector(sFileName, vect);
	if(!bRes)
		return false;

	if(vect.empty())
		return true;

	const size_t nBomSize = sizeof(BOM_UTF8)-1; // 3
	if(vect.size() >= nBomSize)
	{
		if(vect[0] == BYTE(BOM_UTF8[0]) && vect[1] == BYTE(BOM_UTF8[1]) && vect[2] == BYTE(BOM_UTF8[2]))
		{
			// This is UTF-8 file!
			strString = std::string((LPCSTR)&vect[nBomSize], vect.size()-nBomSize);
			return true;
		}
	}
	strString = std::string((LPCSTR)&vect[0], vect.size());
	return true;
}

//------------------------------------------------------

bool SaveTextFileFromString(const tstring& sFileName, const std::string& strString)
{
	if(!PrepareBeforeCreatingFile(sFileName))
		return false;

	return !!SaveBinaryFile(sFileName, strString.data(), strString.size());
}

//------------------------------------------------------

tstring GetExeFullName(void) // 'c:\folder\program.exe'
{
	TCHAR szExe[MAX_PATH] = _T("");
	GetModuleFileName( NULL, szExe, MAX_PATH );
	return szExe;
}

//------------------------------------------------------

tstring GetExeName(void) // 'c:\folder\program.exe' -> 'program'
{
	TCHAR szExe[MAX_PATH] = _T("");
	GetModuleFileName( NULL, szExe, MAX_PATH );
	LPCTSTR szFileName = PathFindFileName(szExe);
	LPCTSTR szExtension = PathFindExtension(szExe);
	return tstring(szFileName, szExtension - szFileName);
}

//------------------------------------------------------

tstring GetExeFolder(void) // With trailing '\'
{
	TCHAR szExe[MAX_PATH] = _T("");
	GetModuleFileName( NULL, szExe, MAX_PATH );
	LPTSTR p = _tcsrchr( szExe, _T('\\') );
	if( p != NULL )
	{
		p[1] = _T('\0');
	}
	return szExe;
}

//------------------------------------------------------

tstring GetExeRelativePath(const tstring& sRelativePath)
{
	if(sRelativePath.length() >= MAX_PATH)
		return sRelativePath;

	if(!PathIsRelative(sRelativePath.c_str()))
		return sRelativePath;

	TCHAR modulePath[MAX_PATH] = _T("");
	GetModuleFileName(NULL, modulePath, MAX_PATH);
	PathRemoveFileSpec(modulePath);

	TCHAR fullPath[MAX_PATH] = _T("");
	if(!PathCombine(fullPath, modulePath, sRelativePath.c_str()))
		return sRelativePath;

	return fullPath;
}

//------------------------------------------------------

tstring MyExpandEnvironmentStrings(const tstring& sFileName)
{
	TCHAR buf[32768] = _T("");
	const DWORD ret = ExpandEnvironmentStrings(sFileName.c_str(), buf, sizeof(buf)/sizeof(buf[0]));
	if(0 == ret || ret >= sizeof(buf)/sizeof(buf[0]))
		return tstring();
	return tstring(buf, ret-1);
}

//------------------------------------------------------

tstring FormatSamplesFolderRelativePath(const tstring& path)	// i.e. "Documentation" folder
{
	if( path.length() >= MAX_PATH)
		return path;

	if(!PathIsRelative(path.c_str()))
		return path;

	TCHAR modulePath[MAX_PATH] = _T("");
	GetModuleFileName(NULL, modulePath, MAX_PATH);
	PathRemoveFileSpec(modulePath);

	TCHAR fullPath1[MAX_PATH] = _T("");

	if(!PathCombine(fullPath1, modulePath, _T("..\\Documentation")))
		return path;

	TCHAR fullPath2[MAX_PATH] = _T("");

	if(!PathCombine(fullPath2, fullPath1, path.c_str()))
		return path;

	return fullPath2;
}

//------------------------------------------------------

tstring GetWindowsFolder(void)
{
	TCHAR szDir[MAX_PATH] = _T("");
	const bool bRes = !!GetWindowsDirectory(szDir, sizeof(szDir)/sizeof(szDir[0]));
	MYASSERT(bRes && "GetWindowsDirectory failed!");
	return szDir;
}

//------------------------------------------------------

bool IsFileExists(const tstring& sFileName)
{
	const DWORD attr = GetFileAttributes(sFileName.c_str());
	if(INVALID_FILE_ATTRIBUTES == attr)
	{
		return false;
	}
	return 0 == (attr&FILE_ATTRIBUTE_DIRECTORY);
}

//------------------------------------------------------

bool IsFolderExists(const tstring& sFileName)
{
	const DWORD attr = GetFileAttributes(sFileName.c_str());
	if(INVALID_FILE_ATTRIBUTES == attr)
	{
		return false;
	}
	return 0 != (attr&FILE_ATTRIBUTE_DIRECTORY);
}

//------------------------------------------------------

GlobalCriticalSection g_protectSaveToLog;

void SaveToLog(const std::string& sLogRecord, const tstring& sExeRelativePath)
{
	const tstring sFileName = GetExeRelativePath(sExeRelativePath);

	SafeCSLock lock(g_protectSaveToLog);

	FILE* const f = my_fopen_aplus(sFileName, _T("ab+"));
	if(NULL != f)
	{
		fwrite(sLogRecord.data(), sLogRecord.size()*sizeof(sLogRecord[0]), 1, f);
		fclose(f);
	}
	else
	{
		MYTRACE("ERROR: Failed opening log file: %s\n", sFileName.c_str());
		MYASSERT(FALSE && "Opening log file failed!");
	}
}

//------------------------------------------------------

SafeCriticalSection	Config::m_protect;

//------------------------------------------------------

Config::Config(const tstring& sIniFile) // using sIniFile == "" for main program's INI
{
	SafeCSLock lock(m_protect);
	Initialize(sIniFile);
}

//------------------------------------------------------

void Config::Initialize(const tstring& sIniFile) // using sIniFile == "" for main program's INI
{
	SafeCSLock lock(m_protect);
	if(_T("") == sIniFile)
	{
		TCHAR buf[MAX_PATH] = _T("");
		GetModuleFileName(NULL, buf, MAX_PATH);
		PathRenameExtension(buf, _T(".ini"));
		m_sIniFileName = buf;
	}
	else
	{
		m_sIniFileName = sIniFile;
	}
}

//------------------------------------------------------

void Config::DeleteValue(const tstring& sSection, const tstring& sKey)
{
	SafeCSLock lock(m_protect);
	WritePrivateProfileString(sSection.c_str(), sKey.c_str(), NULL, m_sIniFileName.c_str());
}

//------------------------------------------------------

void Config::SetString(const tstring& sSection, const tstring& sKey, const tstring& sValue)
{
	SafeCSLock lock(m_protect);
	WritePrivateProfileString(sSection.c_str(), sKey.c_str(), sValue.c_str(), m_sIniFileName.c_str());
}

//------------------------------------------------------

void Config::SetInt(const tstring& sSection, const tstring& sKey, const int nValue)
{
	SafeCSLock lock(m_protect);
	const tstring sNumber = itot(nValue);
	WritePrivateProfileString(sSection.c_str(), sKey.c_str(), sNumber.c_str(), m_sIniFileName.c_str());
}

//------------------------------------------------------

tstring Config::GetString(const tstring& sSection, const tstring& sKey, const tstring& sDefaultValue) const
{
	SafeCSLock lock(m_protect);
	TCHAR szValue[1024*10] = _T("");

	GetPrivateProfileString(sSection.c_str(), sKey.c_str(), sDefaultValue.c_str(),
		szValue, sizeof(szValue)/sizeof(szValue[0]), m_sIniFileName.c_str());
	return szValue;
}

//------------------------------------------------------

int Config::GetInt(const tstring& sSection, const tstring& sKey, const int nDefaultValue) const
{
	SafeCSLock lock(m_protect);
	const int res = GetPrivateProfileInt(sSection.c_str(), sKey.c_str(), nDefaultValue,
		m_sIniFileName.c_str());
	return res;
}

//------------------------------------------------------

void Config::GetStringArray(const tstring& sSection, const tstring& sKey, std::vector<tstring>& vect, const size_t nStartIndex) const
{
	SafeCSLock lock(m_protect);
	vect.clear();
	for(size_t i = nStartIndex; ; i++)
	{
		const tstring sNumber = itot(i);
		const tstring sKeyName = sKey + sNumber;
		const tstring s = GetString(sSection, sKeyName, _T(""));
		if(s.empty())
		{
			break;
		}
		vect.push_back(s);
	}
}

//------------------------------------------------------

void Config::SetStringArray(const tstring& sSection, const tstring& sKey, const std::vector<tstring>& vect, const size_t nStartIndex)
{
	SafeCSLock lock(m_protect);
	for(size_t i = 0; i <= vect.size(); i++)
	{
		const tstring sNumber = itot(i + nStartIndex);
		const tstring sKeyName = sKey + sNumber;
		if(i == vect.size())
		{
			DeleteValue(sSection, sKeyName);
		}
		else
		{
			SetString(sSection, sKeyName, vect[i]);
		}
	}
}

//------------------------------------------------------

tstring Config::GetIniFileName() const
{
	return m_sIniFileName;
}

//------------------------------------------------------

bool GetIniSectionList(const tstring& sIniFile, std::vector<tstring>& vectSections)
{
	SafeCSLock lock(Config::m_protect);

	vectSections.clear();
	std::vector<TCHAR> vectBuffer;
	vectBuffer.resize(100*1024);

	// MSDN: A pointer to a buffer that receives the section names associated with the named file.
	// The buffer is filled with one or more null-terminated strings; the last string is followed
	// by a second null character.

	const DWORD dwRes = GetPrivateProfileSectionNames(&vectBuffer[0], vectBuffer.size(), sIniFile.c_str());

	// MSDN: The return value specifies the number of characters copied to the specified buffer,
	// not including the terminating null character. If the buffer is not large enough to 
	// contain all the section names associated with the specified initialization file, the return
	// value is equal to the size specified by nSize minus two.
	const DWORD dwBadValue = vectBuffer.size() - 2;

	if(dwRes >= dwBadValue)
	{
		MYTRACE("ERROR: Too long INI section list! Ret: %d\n", dwRes);
		MYASSERT(dwRes < dwBadValue);
		return false;
	}

	size_t pos = 0;

	while(pos < vectBuffer.size()-1)
	{
		const tstring s = &vectBuffer[pos];
		if(s.empty())
			break;
		pos += s.length() + 1;
		vectSections.push_back(s);
	}

	vectBuffer.clear();
	return true;
}

//------------------------------------------------------

void CMyFile::AppendTextWithOpeningFile(const tstring& sFileName, const std::string& sAppendText)
{
	SafeCSLock lock(m_protectFile);
	if(NULL == m_pFile)
	{
		m_pFile = my_fopen_aplus(sFileName, _T("ab+"));
	}

	if(NULL != m_pFile)
	{
		fwrite(sAppendText.c_str(), sAppendText.length()*sizeof(sAppendText[0]), 1, m_pFile);
		fflush(m_pFile);
	}
	else
	{
		MYTRACE("ERROR! Couldn't open file '%s', err = %d\n", sFileName.c_str(), GetLastError());
		MYASSERT(FALSE && "Can't open file! (IGNORE IS IGNORE)");
	}
}

//------------------------------------------------------

void CMyFile::AppendTextWithOpeningFile(const tstring& sFileName, const std::wstring& sAppendText)
{
	AppendTextWithOpeningFile(sFileName, UnicodeToUtf8(sAppendText));
#if 0
	SafeCSLock lock(m_protectFile);
	if(NULL == m_pFile)
	{
		m_pFile = my_fopen_aplus(sFileName, _T("ab+"));
	}

	if(NULL != m_pFile)
	{
		fprintf(m_pFile, "%s", sAppendText.c_str());
		//fwrite(sAppendText.c_str(), sAppendText.length()*sizeof(sAppendText[0]), 1, m_pFile);
		fflush(m_pFile);
	}
	else
	{
		MYTRACE("ERROR! Couldn't open file '%s', err = %d\n", sFileName.c_str(), GetLastError());
		MYASSERT(FALSE && "Can't open file! (IGNORE IS IGNORE)");
	}
#endif
}

//------------------------------------------------------
//------------------------------------------------------

