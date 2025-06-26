cmake_minimum_required(VERSION 3.18)

project(DancerAudioPlayer LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR})

# Find Qt6 packages
find_package(Qt6 REQUIRED COMPONENTS
    Widgets
    Multimedia
)

# Enable auto-processing of .ui, .qrc, and .moc files
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

# Source files
set(SOURCES
    main.cpp
    mainWindow.cpp
)

set(HEADERS
    mainWindow.h
)

set(UIS
    mainWindow.ui
)

# Create executable
add_executable(${PROJECT_NAME}
    ${SOURCES}
    ${HEADERS}
    ${UIS}
)

# Link Qt6 libraries
target_link_libraries(${PROJECT_NAME}
    Qt6::Widgets
    Qt6::Multimedia
)
