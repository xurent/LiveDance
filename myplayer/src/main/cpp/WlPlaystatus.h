//
// Created by hxuro on 2019/11/21.
//

#ifndef WY_MUSIC_WLPLAYSTATUS_H
#define WY_MUSIC_WLPLAYSTATUS_H


class WlPlaystatus {

public:
    bool exit = false;
    bool load = true;
    bool seek = false;
    bool pause = false;
public:
    WlPlaystatus();

    ~WlPlaystatus();
};


#endif //WY_MUSIC_WLPLAYSTATUS_H
