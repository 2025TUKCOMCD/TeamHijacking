package com.example.front.iot.smartHome

data class Component(  //iot 기기의 아이디 받아 오는 부분
    val id: String,
    val capabilities: List<Capability>  //한 곳에... 이어진다? Component 부분에 있는 것이, 함께 들어 있어도 되는데
                                        //뭔가를 받아올 때에 한 번에 받아오기 위해 구현한 것으로 추정
)