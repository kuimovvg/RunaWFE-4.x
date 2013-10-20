#!/bin/bash
# В данном файле задаются переменные, рабочие каталоги и файлы для всех тест-сьютов
#
# Переключение режима отладки при запуске тест-сьюта (off - отладка выключена, on - включена)
export selDebug=on
# Базовый URL для тестируемого приложения
export selStartURL=http://10.10.31.35:8080/web-bpm/login
# Путь к файлу Selenium-server
export selServer=$DIRNAME/selenium-server-standalone-2.25.0.jar
# Пути к файлам с тест-сьютами для запуска различных типов тестов
export selSuiteFile=$DIRNAME/../runaex_Test_suite_CRUD.html
# Путь к файлу-результату, в который скидывается лог работы Selenium
export selResultFile=../log/testResult.html
# Путь к файлу с отладочной информацией, в который скидывается лог, если параметр selDebug=on
export selDebugFile=../log/selDebug.txt
# Строка браузера или соответствующая переменная окружения
export selBrowserString=*firefox
# Путь к профилю Мозиллы
export ffProfile=$DIRNAME/runaex_ff_profile/
# Java-Script файл с расширениями функционала Selenium (с названием user-extensions.js)
export selExtensions=$DIRNAME/../ext/user-extensions.js
# Номер порта для сервера Selenium (по умолчанию 4444)
export selPort=4444
# Таймаут в миллисекундах для команд Selenium (по умолчанию 50000)
export selTimeout=250000
# Обязательный параметр для виртуального дисплея Xvfb
export DISPLAY=:1