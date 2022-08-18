package com.example

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route

@Route("")
class TestView extends Div {

    TestView() {
        text = "test"
    }
}
