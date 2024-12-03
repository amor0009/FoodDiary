cmake_minimum_required(VERSION 3.18.1)

# Укажите имя библиотеки
add_library(date_format SHARED date_format.c)

# Подключите стандартную библиотеку логирования Android
find_library(log-lib log)

# Свяжите библиотеку с логированием (если требуется)
target_link_libraries(date_format ${log-lib})
