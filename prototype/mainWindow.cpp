#include "mainWindow.h"
#include "ui_mainWindow.h"

#include <QMediaPlayer>
#include <QAudioOutput>
#include <QFileDialog>
#include <QTimer>
#include <algorithm>
#include <iomanip>
#include <sstream>

namespace{
    QString msToTime(qint64 ms){
        int totalSec = static_cast<int>(ms/1000);
        int minutes = totalSec/60;
        int seconds = totalSec%60;
        return QString("%1:%2").arg(minutes, 2, 10, QChar('0')).arg(seconds, 2, 10, QChar('0'));
    }
}

MainWindow::MainWindow(QWidget *parent):
    QMainWindow(parent), 
    ui(new Ui::MainWindow),
    player(new QMediaPlayer(this)), 
    audioOut(new QAudioOutput(this)){
        
        ui->setupUi(this);

        player->setAudioOutput(audioOut);
        audioOut->setVolume(1.0);

        ui->playPauseButton->setText("Play");
        ui->loopButton->setText("loop");
        ui->loopButton->setCheckable(true);
        ui->timeLabel->setText("0:00/0:00");
        ui->speedControl->setValue(1.0);

        QTimer::singleShot(0, this, [this]{
            const QString file = QFileDialog::getOpenFileName(
                this, "Open audio file", QDir::homePath(), 
                "Audio files (*.mp3 *.wav *.flac *.ogg)");
                if (!file.isEmpty())
                    player->setSource(QUrl::fromLocalFile(file));
        });

        connect (ui->playPauseButton, &QPushButton::clicked,
                this, &MainWindow::togglePlayPause);
        connect (ui->loopButton, &QPushButton::toggled,
                this, &MainWindow::toggleLoop);
        connect (ui->addMarkerButton, &QPushButton::clicked,
                this, &MainWindow::addMarker);
        connect (ui->prevMarkerButton, &QPushButton::clicked,
                this, &MainWindow::goPrevMarker);
        connect (ui->nextMarkerButton, &QPushButton::clicked,
                this, &MainWindow::goNextMarker);
        connect (ui->positionSlider, &QSlider::sliderMoved,
                player, &QMediaPlayer::setPosition);
        connect (ui->speedControl, QOverload<double>::of(&QDoubleSpinBox::valueChanged),
                player, &QMediaPlayer::setPlaybackRate);
        connect (player, &QMediaPlayer::durationChanged, 
                this, &MainWindow::whenDurationChanged);
        connect (player, &QMediaPlayer::positionChanged,
                this, &MainWindow::whenPositionChanged);
}
    
MainWindow::~MainWindow(){
    delete ui;
}

void MainWindow::togglePlayPause(){
    if(player->playbackState() == QMediaPlayer::PlayingState){
        player->pause();
        ui->playPauseButton->setText("Play");
    }
    else{
        player->play();
        ui->playPauseButton->setText("Pause");
    }
}

void MainWindow::toggleLoop(bool enabled){
    loopActive = enabled;
    ui->loopButton->setStyleSheet(enabled ? "background-color: #4caf50;" : "");
}

void MainWindow::addMarker(){
    markers.push_back(player->position());
    std::sort(markers.begin(), markers.end());
    refreshMarkerList();
}

void MainWindow::goPrevMarker(){
    const qint64 pos = player->position();
    auto it = std::lower_bound(markers.begin(), markers.end(), pos);
    if (it == markers.begin()) return;
    player->setPosition(*(--it));
}

void MainWindow::goNextMarker(){
    const qint64 pos = player->position();
    auto it = std::lower_bound(markers.begin(), markers.end(), pos);
    if (it == markers.end()) return;
    player->setPosition(*it);
}

void MainWindow::whenDurationChanged(qint64 dur){
    ui->positionSlider->setRange(0, static_cast<int>(dur));
    ui->timeLabel->setText(QString("0:00 / %1").arg(msToTime(dur)));
}

void MainWindow::whenPositionChanged(qint64 pos){
    if(!ui->positionSlider->isSliderDown()){
        ui->positionSlider->setValue(static_cast<int>(pos));
    }
    ui->timeLabel->setText(QString("%1 / %2").arg(msToTime(pos)).arg(msToTime(player->duration())));
    if (loopActive && player->playbackState() == QMediaPlayer::PlayingState && pos >= player->duration() - 100){
        player->setPosition(0);
    }
}

void MainWindow::refreshMarkerList(){
    ui->markerList->clear();
    for (qint64 m : markers){
        ui->markerList->addItem(msToTime(m));
    }
}

