#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QMediaPlayer>
#include <QAudioOutput>
#include <vector>

QT_BEGIN_NAMESPACE
namespace Ui {class MainWindow;}
QT_END_NAMESPACE

class MainWindow : public QMainWindow{
    Q_OBJECT
    public:
        explicit MainWindow(QWidget *parent = nullptr);
        ~MainWindow();
    private slots:
        void togglePlayPause();
        void toggleLoop(bool enabled);
        void addMarker();
        void goNextMarker();
        void goPrevMarker();
        void whenDurationChanged(qint64 dur);
        void whenPositionChanged(qint64 pos);
        void refreshMarkerList();
    private:
        Ui::MainWindow *ui;
        QMediaPlayer *player;
        QAudioOutput *audioOut;
        std::vector <qint64> markers;
        bool loopActive = false;
};

#endif
