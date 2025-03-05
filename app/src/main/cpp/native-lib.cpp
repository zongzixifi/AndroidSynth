#include <jni.h>
#include <string>
#include "fluidsynth/include/fluidsynth.h"
#include "fluidsynth/include/fluidsynth/midi.h"
#include "fluidsynth/include/fluidsynth/event.h"
#include <android/log.h>
#include <vector>
#include <chrono>
#include <__thread/this_thread.h>
#include <thread>
#include <condition_variable>
#include <map>

#define LOG_TAG "FluidSynthTest"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

fluid_settings_t *settings = nullptr;
fluid_synth_t *synth = nullptr;
fluid_audio_driver_t *adriver = nullptr;
short buffer[2048];

int preset_number = 25; // 鼓组设置
std::mutex mtx;
std::condition_variable cv;

fluid_sequencer_t *seq;

short synth_id,mySeqID;
short recorder_id;
int BPM = 60;
int bar = 4;
int clap = 4;
int loopLengthMs = bar*clap/(BPM/60) * 1000; // 4 小节长度 (120 BPM)
int count = 0; // 节拍器计数
unsigned int startTime;

bool isRecording = false;
bool isPlaying = false;
bool stopThread = false;
bool ifMetronomeON = false;

struct MidiEvent {
    unsigned int time;
    int note;
    int velocity;
    bool isNoteOn;
};

std::vector<MidiEvent> midiTrack;
std::map<std::pair<int, int>, std::vector<MidiEvent>> DrumMidiTrack;

void midi_record_callback(unsigned int time, int note, int velocity, bool isNoteOn) {
    // 记录 MIDI 事件
    if (isRecording) {
        midiTrack.push_back({time, note, velocity, isNoteOn});
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Recorded MIDI Event: Time=%d, Note=%d, Vel=%d, On=%d",
                            time, note, velocity, isNoteOn);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_setDrumNote(JNIEnv *env, jobject /* this */, jint note, jint timeNum, jint svel) {
    unsigned int time = timeNum * (60/(BPM) * 1000)/4 ;

    DrumMidiTrack[{timeNum, note}].push_back({time, note, svel, true});
    DrumMidiTrack[{timeNum, note}].push_back({time+(60/(BPM) * 1000)/4, note, svel, false});
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Recorded drum MIDI Event: Time=%d, Note=%d, Vel=%d, On=%d",
                        timeNum, note, svel, true);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_delDrumNote(JNIEnv *env, jobject /* this */, jint note , jint timeNum) {
    unsigned int time = timeNum * (60/(BPM) * 1000)/4 ;

    auto key = std::make_pair(timeNum, note);
    auto it = DrumMidiTrack.find(key);
    if (it != DrumMidiTrack.end()) {
        DrumMidiTrack.erase(it);
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Delete drum MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Not found drum MIDI Event: Time=%d, Note=%d, On=%d",
                            timeNum, note, true);
    }
}



void schedule_note_on(short note, int channel, short velocity, unsigned int delay_ms) {
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, synth_id);
    fluid_event_noteon(event, channel, note, velocity);
    int sendat = fluid_sequencer_send_at(seq, event, fluid_sequencer_get_tick(seq) + delay_ms, 1);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "fluid_sequencer_send_at return %d note: %d velocity:%d", sendat, note, velocity);
    delete_fluid_event(event);
}


void schedule_note_off(short note, int channel,unsigned int delay_ms) {
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, synth_id);
    fluid_event_noteoff(event, channel,note);

    int sendat = fluid_sequencer_send_at(seq, event, fluid_sequencer_get_tick(seq) + delay_ms, 1);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "fluid_sequencer_send_at return %d", sendat);
    delete_fluid_event(event);
}

void loop_drum_midi_events() {
    for (const auto& [key, events] : DrumMidiTrack) {
        for (const auto& event : events) {
            if (event.isNoteOn) {
                schedule_note_on(event.note, 9, event.velocity, event.time);
            } else {
                schedule_note_off(event.note, 9,   event.time);
            }
        }
    }
}

void play_drum(){
    if(count % clap == 0){
        loop_drum_midi_events();
    }
}

void playMetronome(){
    if(ifMetronomeON) {
        if (count == 0){
            short note = 80;
            short velocity = 120;
            schedule_note_on(note, 0, velocity, 0);  // 立即触发 Note On
            schedule_note_off(note, 0, 500);
        }else {
            short note = 60;
            short velocity = 100;
            schedule_note_on(note, 0, velocity, 0);  // 立即触发 Note On
            schedule_note_off(note, 0, 500);
        }
    }
}

void loop_midi_events() {
    for (const auto &event : midiTrack) {
        if (event.isNoteOn) {
            schedule_note_on(event.note,1, event.velocity, event.time);
        } else {
            schedule_note_off(event.note,1, event.time);
        }
    }
}

void playBack(){
    if(isPlaying){
        if(count == 0){
            loop_midi_events();
        }
    }
}

// 定义回调函数, 起到确定时序的作用，每拍触发一次，播放节拍器，每四小节开头写入下一轮音符。可以加入重置时序功能
void timer_callback(unsigned int time, fluid_event_t *event, fluid_sequencer_t *seq, void *data) {
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "Timer Event Triggered at time %u", time);


    playMetronome(); // 播放节拍器
    playBack();
    play_drum();
    count = (count + 1) % (bar*clap);
    // 继续发送下一个定时事件，实现周期性回调
    fluid_event_t *new_event = new_fluid_event();
    fluid_event_set_source(new_event, -1);
    fluid_event_set_dest(new_event, mySeqID);
    fluid_event_timer(new_event, nullptr);

    // 设定下一次回调的时间
    unsigned int next_time = fluid_sequencer_get_tick(seq) + loopLengthMs/(bar*clap); // 每拍回调一次
    fluid_sequencer_send_at(seq, new_event, next_time, 1);

    delete_fluid_event(new_event);
}

//启动定时器，loop循环时序的起始位置设置
void startTimer() {
    if (seq == nullptr) return;

    // 创建一个定时事件
    fluid_event_t *event = new_fluid_event();
    fluid_event_set_source(event, -1);
    fluid_event_set_dest(event, mySeqID);
    fluid_event_timer(event, nullptr);
    startTime = fluid_sequencer_get_tick(seq) + loopLengthMs/(bar*clap); //loop开始事件 当前+一拍
    // 发送定时事件，触发首次回调
    fluid_sequencer_send_at(seq, event, startTime, 1);

    delete_fluid_event(event);
}

void print_option( void *data, const char *name, const char *option){
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "Available %s: %s", name, option);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_project2_FluidSynthManager_createFluidSynth(JNIEnv *env, jobject thiz) {
    // 检查是否已经初始化，防止重复创建
    if (synth != nullptr) {
        return env->NewStringUTF("FluidSynth already initialized");
    }
    // 创建 FluidSynth 组件
    settings = new_fluid_settings();
    synth = new_fluid_synth(settings);
    //  加载 SoundFont
    int sfid = fluid_synth_sfload(synth, "/data/data/com.example.project2/files/soundfont.sf2", 1);
    if (sfid == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "FluidSynth", "Failed to load SoundFont");
        return env->NewStringUTF("Failed to load SoundFont");
    }
    fluid_synth_program_change(synth, 1, 6);
    fluid_synth_program_select(synth, 9,  sfid, 120, preset_number);

    fluid_settings_setstr(settings, "audio.driver", "oboe");
    adriver = new_fluid_audio_driver(settings, synth);

    seq = new_fluid_sequencer2(0);
    synth_id = fluid_sequencer_register_fluidsynth(seq, synth);
    //recorder_id = fluid_sequencer_register_client(seq, "MIDI Recorder", midi_record_callback, nullptr);
    mySeqID = fluid_sequencer_register_client(seq, "timer", timer_callback, nullptr);
    //synth_id = fluid_sequencer_register_client(seq, "MIDI Handler", midi_event_callback, synth);
    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "FluidSynth initialized successfully");
    fluid_settings_foreach_option(settings, "audio.driver", nullptr, print_option);
    startTimer();
    return env->NewStringUTF("FluidSynth initialized successfully");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_destoryFluidSynth(JNIEnv *env, jobject thiz) {
    delete_fluid_audio_driver(adriver);
    delete_fluid_sequencer(seq);
    delete_fluid_synth(synth);

    __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "FluidSynth delete successfully");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_playNote(JNIEnv *env, jobject /* this */, jint note, jint svel , jint channel) {
    auto snote = (short)note;
    auto vel = (short)svel;
    if (synth != nullptr) {
        schedule_note_on(snote, channel,vel, 0);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "playNote");
        if(isRecording){
            unsigned int time = (fluid_sequencer_get_tick(seq)-startTime) % loopLengthMs;
            midi_record_callback(time, snote, vel, true);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopNote(JNIEnv *env, jobject /* this */, jint note, jint channel) {
    auto snote = (short)note;
    if (synth != nullptr) {
        schedule_note_off(snote, channel, 0);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "stopNote");
        if(isRecording){
            unsigned int time = (fluid_sequencer_get_tick(seq)-startTime) % loopLengthMs;
            midi_record_callback(time, snote,0, false);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopNoteDelay(JNIEnv *env, jobject /* this */, jint note, jint channel) {
    auto snote = (short)note;
    if (synth != nullptr) {
        schedule_note_off(snote,1, 50);
        __android_log_print(ANDROID_LOG_INFO, "FluidSynth", "stopNote");
    }
}
/*
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_playMidiLoop(JNIEnv *env, jobject ) {
    if (synth == nullptr || seq == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "FluidSynth", "Synthesizer or Sequencer not initialized");
        return;
    }
    isPlaying = false;

    std::thread([]() {
        while (!stopThread) {
            std::unique_lock<std::mutex> lock(mtx);
            cv.wait(lock, [] { return isPlaying || stopThread; });

            __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "playMidiLoop started: isPlaying=%d", isPlaying);

            if (stopThread) break;

            loop_midi_events();
            std::this_thread::sleep_for(std::chrono::milliseconds(loopLengthMs));
        }
    }).detach();
}
*/

//开启节拍器
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_turnMetronomeON(JNIEnv *env, jobject) {
    ifMetronomeON = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StartRecording");
}

//关闭节拍器
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_turnMetronomeOff(JNIEnv *env, jobject) {
    ifMetronomeON = false;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StartRecording");
}

//开始录制
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startRecording(JNIEnv *env, jobject) {
    isRecording = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StartRecording");
}
//停止录制
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopRecording(JNIEnv *env, jobject) {
    isRecording = false;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "StopRecording");

}
//停止回放
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopPlayback(JNIEnv *env, jobject) {
    {
        std::lock_guard<std::mutex> lock(mtx);
        isPlaying = false;
    }
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "stopPlayback, isPlaying=%d", isPlaying);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startPlayback(JNIEnv *env, jobject) {
    {
        std::lock_guard<std::mutex> lock(mtx);
        isPlaying = true;
    }
    cv.notify_one();  // 唤醒等待的线程
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "startPlayback, isPlaying=%d", isPlaying);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_startOverdub(JNIEnv *env, jobject) {
    isPlaying = true;
    isRecording = true;
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "startOverdub, isPlaying=%d, isRecording=%d", isPlaying, isRecording);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_clearLoop(JNIEnv *env, jobject) {
    midiTrack.clear();
    __android_log_print(ANDROID_LOG_DEBUG, "FluidSynth", "clearLoop");
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_project2_FluidSynthManager_stopThread(JNIEnv *env, jobject) {
    {
        std::lock_guard<std::mutex> lock(mtx);
        stopThread = true;
    }
    cv.notify_one();
}