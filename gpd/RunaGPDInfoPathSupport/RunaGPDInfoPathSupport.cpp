// RunaGPDInfoPathSupport.cpp : Defines the entry point for the DLL application.
//
#include <windows.h>
//#include <stdlib.h>
//#include <string.h>
#include <io.h>
#include <iostream.h>
#include <fcntl.h>
#include <sys/stat.h>

#include "fci.h"
#include "fdi.h"
#include "org_jbpm_ui_util_InfoPathSupport.h"

#define MEDIA_SIZE			300000
#define FOLDER_THRESHOLD	900000
#define COMPRESSION_TYPE    tcompTYPE_MSZIP


char *return_fdi_error_string(FDIERROR err);
char *return_fci_error_string(FCIERROR err);

/*
 * Destination directory for extracted files
 */
char* fileDir;
char* sourceFolder;
char* extractedFileDir;
char* extractedFileName;
bool extractAll = false;

void ThrowException(JNIEnv *env, const char* desc) {
  jclass failClass = env->FindClass("java/lang/Exception");
  cout << "failClass = " << failClass;
  env->ThrowNew(failClass, desc);
}

BOOL APIENTRY DllMain( HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
    return TRUE;
}

FNALLOC(mem_alloc) {
	return malloc(cb);
}

FNFREE(mem_free) {
	free(pv);
}


FNOPEN(file_open) {
	return _open(pszFile, oflag, pmode);
}


FNREAD(file_read) {
	return _read(hf, pv, cb);
}


FNWRITE(file_write) {
	return _write(hf, pv, cb);
}


FNCLOSE(file_close) {
	return _close(hf);
}


FNSEEK(file_seek) {
	return _lseek(hf, dist, seektype);
}

FNFCIALLOC(mem_allocCI) {
	return malloc(cb);
}

FNFCIFREE(mem_freeCI) {
	free(memory);
}


FNFCIOPEN(fci_open) {
    int result = _open(pszFile, oflag, pmode);
    if (result == -1) *err = errno;
    return result;
}

FNFCIREAD(fci_read) {
    unsigned int result = (unsigned int) _read(hf, memory, cb);
    if (result != cb) *err = errno;
    return result;
}

FNFCIWRITE(fci_write) {
    unsigned int result = (unsigned int) _write(hf, memory, cb);
    if (result != cb) *err = errno;
    return result;
}

FNFCICLOSE(fci_close) {
    int result = _close(hf);
    if (result != 0) *err = errno;
    return result;
}

FNFCISEEK(fci_seek) {
    long result = _lseek(hf, dist, seektype);
    if (result == -1) *err = errno;
    return result;
}

FNFCIDELETE(fci_delete) {
    int result = remove(pszFile);
    if (result != 0) *err = errno;
    return result;
}


/*
 * File placed function called when a file has been committed
 * to a cabinet
 */
FNFCIFILEPLACED(file_placed) {
	cout << "added file '" << pszFile << "' (size " << cbFile << ") on cab \n";
	return 0;
}


/*
 * Function to obtain temporary files
 */
FNFCIGETTEMPFILE(get_temp_file) {
    char *psz;
    psz = _tempnam("","xx");            // Get a name
    if ((psz != NULL) && (strlen(psz) < (unsigned)cbTempName)) {
        strcpy(pszTempName,psz);        // Copy to caller's buffer
        free(psz);                      // Free temporary name buffer
        return TRUE;                    // Success
    }
    //** Failed
    if (psz) {
        free(psz);
    }
    return FALSE;
}

FNFCISTATUS(progress) {
	return 0;
}	

FNFCIGETOPENINFO(get_open_info) {BY_HANDLE_FILE_INFORMATION	finfo; FILETIME	filetime; HANDLE handle; DWORD attrs; int hf;
    /*
     * Need a Win32 type handle to get file date/time
     * using the Win32 APIs, even though the handle we
     * will be returning is of the type compatible with
     * _open
     */
	handle = CreateFile(pszName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL | FILE_FLAG_SEQUENTIAL_SCAN, NULL);
	if (handle == INVALID_HANDLE_VALUE) {
		return -1;
	}
	if (GetFileInformationByHandle(handle, &finfo) == FALSE) {
		CloseHandle(handle);
		return -1;
	}
   
	FileTimeToLocalFileTime(&finfo.ftLastWriteTime, &filetime);
	FileTimeToDosDateTime(&filetime, pdate, ptime);
    attrs = GetFileAttributes(pszName);
    if (attrs == 0xFFFFFFFF) {
        /* failure */
        *pattribs = 0;
    } else {
        /*
         * Mask out all other bits except these four, since other
         * bits are used by the cabinet format to indicate a
         * special meaning.
         */
        *pattribs = (int) (attrs & (_A_RDONLY | _A_SYSTEM | _A_HIDDEN | _A_ARCH));
    }
    CloseHandle(handle);
    /*
     * Return handle using _open
     */
	hf = _open( pszName, _O_RDONLY | _O_BINARY );
	if (hf == -1)
		return -1; // abort on error
	return hf;
}

FNFDINOTIFY(notification_function) {
	switch (fdint)
	{
		case fdintCABINET_INFO: // general information about the cabinet
			return 0;
		case fdintPARTIAL_FILE: // first file in cabinet is continuation
			return 0;
		case fdintCOPY_FILE: {  // file to be copied
			int	handle;
			char destination[256];
			
			if(extractAll || (strcmp(pfdin->psz1, extractedFileName) == 0)) {
				sprintf(destination, "%s%s", extractedFileDir, pfdin->psz1);
				handle = file_open(destination, _O_BINARY | _O_CREAT | _O_WRONLY | _O_SEQUENTIAL, _S_IREAD | _S_IWRITE);
				return handle;
			} else {
				//printf("%s != %s, op = [%d]\n", extractedFileName, pfdin->psz1, strcmp(pfdin->psz1, extractedFileName));
				return 0; /* skip file */
			}
		}

		case fdintCLOSE_FILE_INFO: {  // close the file, set relevant info
            HANDLE handle;
            DWORD attrs;
            char destination[256];

 			cout << "Extracting " << pfdin->psz1 << "\n";
            sprintf(destination, "%s%s", fileDir, pfdin->psz1);
			file_close(pfdin->hf);

            /*
             * Set date/time
             * Need Win32 type handle for to set date/time
             */
            handle = CreateFile(destination, GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

            if (handle != INVALID_HANDLE_VALUE) {
                FILETIME datetime;
                if (TRUE == DosDateTimeToFileTime(pfdin->date, pfdin->time, &datetime)) {
                    FILETIME local_filetime;
                    if (TRUE == LocalFileTimeToFileTime(&datetime, &local_filetime)) {
                        SetFileTime(handle, &local_filetime, NULL, &local_filetime);
                    }
                }
                CloseHandle(handle);
            }

            /*
             * Mask out attribute bits other than readonly,
             * hidden, system, and archive, since the other
             * attribute bits are reserved for use by
             * the cabinet format.
             */
            attrs = pfdin->attribs;
            attrs &= (_A_RDONLY | _A_HIDDEN | _A_SYSTEM | _A_ARCH);
            SetFileAttributes(destination, attrs);
			return TRUE;
        }

		case fdintNEXT_CABINET:	// file continued to next cabinet
			return 0;
	}

	return 0;
}

jbyteArray getStringBytes(JNIEnv *env, jstring str) {
	if(!str) return NULL;
	jmethodID getBytes = env->GetMethodID(env->GetObjectClass(str), "getBytes", "()[B");
	jbyteArray buf = (jbyteArray) env->CallObjectMethod(str, getBytes);
	if(!buf) return NULL;
	// Добавляем ноль-символ
	jsize len = env->GetArrayLength(buf);
	jbyteArray nbuf = env->NewByteArray(len+1);
	if(len != 0) {
		jbyte *cbuf = env->GetByteArrayElements(buf, NULL);
		env->SetByteArrayRegion(nbuf, 0, len, cbuf);
		env->ReleaseByteArrayElements(buf, cbuf, JNI_ABORT);
	}
	env->DeleteLocalRef(buf);
	return nbuf;
}

JNIEXPORT jboolean JNICALL Java_org_jbpm_ui_util_InfoPathSupport_isXSNFileValid(JNIEnv *env, jclass, jstring arg0) {
	jbyteArray bmsg = getStringBytes(env, arg0);
	if(!bmsg) return -1;
	char* cabFilePath = (char*) env->GetByteArrayElements(bmsg, NULL);

	ERF	erf;
	FDICABINETINFO fdici;
	cout << "isXSNFileValid: " << cabFilePath << "\n";
	HFDI hfdi = FDICreate(mem_alloc, mem_free, file_open, file_read, file_write, file_close, file_seek, cpu80386, &erf);
	if (hfdi == NULL) {
		printf("FDICreate() failed: code %d [%s]\n", erf.erfOper, return_fdi_error_string((FDIERROR) erf.erfOper));
		ThrowException(env, cabFilePath);
		return FALSE;
	}

	int	hf = file_open((char*) cabFilePath, _O_BINARY | _O_RDONLY | _O_SEQUENTIAL, 0);

	if (hf == -1) {
		FDIDestroy(hfdi);
		cout << "Unable to open file '" << cabFilePath << "' for input\n";
		ThrowException(env, cabFilePath);
		return FALSE;
	}

	BOOL result = FDIIsCabinet(hfdi, hf, &fdici);
	_close(hf);
	FDIDestroy(hfdi);

	env->ReleaseByteArrayElements(bmsg, (jbyte*) cabFilePath, JNI_ABORT);

	return result;
}

JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_extractFileFromXSN(JNIEnv *env, jclass, jstring arg0, jstring arg1, jstring arg2, jstring arg3) {
	jbyteArray bmsg1 = getStringBytes(env, arg0);
	if(!bmsg1) return;
	fileDir = (char*) env->GetByteArrayElements(bmsg1, NULL);

	jbyteArray bmsg2 = getStringBytes(env, arg1);
	if(!bmsg2) return;
	char* cabFileName = (char*) env->GetByteArrayElements(bmsg2, NULL);

	jbyteArray bmsg3 = getStringBytes(env, arg2);
	if(!bmsg3) return;
	extractedFileDir = (char*) env->GetByteArrayElements(bmsg3, NULL);
	
	jbyteArray bmsg4 = getStringBytes(env, arg3);
	if(!bmsg4) return;
	extractedFileName = (char*) env->GetByteArrayElements(bmsg4, NULL);

	extractAll = false;

	ERF	erf;
	cout << "extractFileFromXSN: [" << fileDir << "] [" << cabFileName << "] [" << extractedFileDir << "] [" << extractedFileName << "] \n";

	HFDI hfdi = FDICreate(mem_alloc, mem_free, file_open, file_read, file_write, file_close, file_seek, cpu80386, &erf);
	if (hfdi == NULL) {
		cout << "FDICreate() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		return;
	}

	if (TRUE != FDICopy(hfdi, cabFileName, fileDir, 0, notification_function, NULL, NULL)) {
		cout << "FDICopy() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		FDIDestroy(hfdi);
		return;
	}

	FDIDestroy(hfdi);
	env->ReleaseByteArrayElements(bmsg1, (jbyte*) fileDir, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg2, (jbyte*) cabFileName, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg3, (jbyte*) extractedFileDir, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg4, (jbyte*) extractedFileName, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_extractAllFilesFromXSN(JNIEnv *env, jclass, jstring arg0, jstring arg1, jstring arg2) {
	jbyteArray bmsg1 = getStringBytes(env, arg0);
	if(!bmsg1) return;
	fileDir = (char*) env->GetByteArrayElements(bmsg1, NULL);

	jbyteArray bmsg2 = getStringBytes(env, arg1);
	if(!bmsg2) return;
	char* cabFileName = (char*) env->GetByteArrayElements(bmsg2, NULL);

	jbyteArray bmsg3 = getStringBytes(env, arg2);
	if(!bmsg3) return;
	extractedFileDir = (char*) env->GetByteArrayElements(bmsg3, NULL);
	
	extractAll = true;

	ERF	erf;
	cout << "extractAllFilesFromXSN: [" << fileDir << "] [" << cabFileName << "] [" << extractedFileDir << "] \n";

	HFDI hfdi = FDICreate(mem_alloc, mem_free, file_open, file_read, file_write, file_close, file_seek, cpu80386, &erf);
	if (hfdi == NULL) {
		cout << "FDICreate() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		return;
	}

	if (TRUE != FDICopy(hfdi, cabFileName, fileDir, 0, notification_function, NULL, NULL)) {
		cout << "FDICopy() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		FDIDestroy(hfdi);
		return;
	}

	FDIDestroy(hfdi);
	env->ReleaseByteArrayElements(bmsg1, (jbyte*) fileDir, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg2, (jbyte*) cabFileName, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg3, (jbyte*) extractedFileDir, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_createInfoPathXSNFile(JNIEnv *env, jclass, jstring arg0, jstring arg1) {
	jbyteArray bmsg1 = getStringBytes(env, arg0);
	if(!bmsg1) return;
	sourceFolder = (char*) env->GetByteArrayElements(bmsg1, NULL);
	jbyteArray bmsg2 = getStringBytes(env, arg1);
	if(!bmsg1) return;
	char* xsnFilePath = (char*) env->GetByteArrayElements(bmsg2, NULL);

	cout << "createInfoPathXSNFile: " << sourceFolder << " " << xsnFilePath << "\n";

	ERF	erf;
	CCAB cab_parameters;
	memset(&cab_parameters, 0, sizeof(CCAB));
	cab_parameters.cb = MEDIA_SIZE;
	cab_parameters.cbFolderThresh = FOLDER_THRESHOLD;
	cab_parameters.cbReserveCFHeader = 0;
	cab_parameters.cbReserveCFFolder = 0;
	cab_parameters.cbReserveCFData   = 0;
	cab_parameters.iDisk = 0;
	cab_parameters.setID = 197;
	strcpy(cab_parameters.szCabPath, xsnFilePath);

	HFCI hfci = FCICreate(&erf, file_placed, mem_allocCI, mem_freeCI, fci_open, fci_read, fci_write, fci_close, fci_seek, fci_delete, get_temp_file, &cab_parameters, NULL);

	if (hfci == NULL) {
		cout << "FCICreate() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		return;
	}

	char filePath[256];
	struct _finddata_t find;
	long h;

	char searchPath[256];
	strcpy(searchPath, sourceFolder);
	strcat(searchPath, "*");

	printf("searchPath: [%s] \n", searchPath);

	h = _findfirst (searchPath, &find); 

	if (h != -1) {
		do {
			if (strlen(find.name) > 2) {
				strcpy(filePath, sourceFolder);
				strcat(filePath, find.name);
				cout << "found and adding: " << filePath << "\n";

				if (FALSE == FCIAddFile(hfci, filePath, find.name, FALSE, NULL, progress, get_open_info, COMPRESSION_TYPE)) {
					cout << "FCIAddFile() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
					FCIDestroy(hfci);
					return;
				}
			}
		} while (_findnext (h, &find) >= 0);
		_findclose(h);
	}

	/*
	 * This will automatically flush the folder first
	 */
	if (FALSE == FCIFlushCabinet(hfci, FALSE, NULL, progress)) {
		cout << "FCIFlushCabinet() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
        FCIDestroy(hfci);
		return;
	}
    if (FCIDestroy(hfci) != TRUE) {
		cout << "FCIDestroy() failed: code " << erf.erfOper << " [" << return_fdi_error_string((FDIERROR) erf.erfOper) << "]\n";
		return;
	}

	env->ReleaseByteArrayElements(bmsg1, (jbyte*) sourceFolder, JNI_ABORT);
	env->ReleaseByteArrayElements(bmsg2, (jbyte*) xsnFilePath, JNI_ABORT);
}

char *return_fdi_error_string(FDIERROR err)
{
	switch (err)
	{
		case FDIERROR_NONE:
			return "No error";
		case FDIERROR_CABINET_NOT_FOUND:
			return "Cabinet not found";
		case FDIERROR_NOT_A_CABINET:
			return "Not a cabinet";
		case FDIERROR_UNKNOWN_CABINET_VERSION:
			return "Unknown cabinet version";
		case FDIERROR_CORRUPT_CABINET:
			return "Corrupt cabinet";
		case FDIERROR_ALLOC_FAIL:
			return "Memory allocation failed";
		case FDIERROR_BAD_COMPR_TYPE:
			return "Unknown compression type";
		case FDIERROR_MDI_FAIL:
			return "Failure decompressing data";
		case FDIERROR_TARGET_FILE:
			return "Failure writing to target file";
		case FDIERROR_RESERVE_MISMATCH:
			return "Cabinets in set have different RESERVE sizes";
		case FDIERROR_WRONG_CABINET:
			return "Cabinet returned on fdintNEXT_CABINET is incorrect";
		case FDIERROR_USER_ABORT:
			return "User aborted";
		default:
			return "Unknown error";
	}
}

char *return_fci_error_string(FCIERROR err) {
	switch (err) {
		case FCIERR_NONE:
			return "No error";
		case FCIERR_OPEN_SRC:
			return "Failure opening file to be stored in cabinet";
		case FCIERR_READ_SRC:
			return "Failure reading file to be stored in cabinet";
		case FCIERR_ALLOC_FAIL:
			return "Insufficient memory in FCI";
		case FCIERR_TEMP_FILE:
			return "Could not create a temporary file";
		case FCIERR_BAD_COMPR_TYPE:
			return "Unknown compression type";
		case FCIERR_CAB_FILE:
			return "Could not create cabinet file";
		case FCIERR_USER_ABORT:
			return "Client requested abort";
		case FCIERR_MCI_FAIL:
			return "Failure compressing data";
		default:
			return "Unknown error";
	}
}


