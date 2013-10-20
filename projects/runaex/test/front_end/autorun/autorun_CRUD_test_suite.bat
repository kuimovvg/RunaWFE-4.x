:: AUTHOR: Т.М. Гильмуллин
:: TEST PRIORITY: 0
:: SPEC ID: -
:: OVERVIEW: Данный .bat-файл запускает тест-сьют runaex_Test_suite_CRUD.html содержащий тест-кейсы для стандартной проверки работоспособности интерфейса.
:: IDEA: Тест-кейсы, вызываемые в runaex_Test_suite_CRUD.html должны быть завершены успешно.
:: SETUP&INFO: Существует файл runaex_Test_suite_CRUD.html оформленный по правилам Selenium. Необходимо верно настроить переменные для рабочих каталогов и файлов.

@echo off

:: ----- Настраиваем переменные окружения, рабочие каталоги и файлы -----
if NOT EXIST ..\log (
	md ..\log
)
call config_for_all_test_suites.bat

:: ----- Запускаем сервер Selenium и тест-сьют на исполнение с указанными параметрами -----
if %selDebug%==on (
	java -jar %selServer% -debug -htmlSuite %selBrowserString% %selStartURL% %selSuiteFile% %selResultFile% -port %selPort% -timeout %selTimeout% -userExtensions %selExtensions% -firefoxProfileTemplate %ffProfile% > %selDebugFile%
)
if %selDebug%==off (
	java -jar %selServer% -htmlSuite %selBrowserString% %selStartURL% %selSuiteFile% %selResultFile% -port %selPort% -timeout %selTimeout% -userExtensions %selExtensions% -firefoxProfileTemplate %ffProfile%
)