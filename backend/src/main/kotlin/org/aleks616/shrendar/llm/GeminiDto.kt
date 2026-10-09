package org.aleks616.shrendar.llm


class GeminiDTO {
    class Request {
        var contents:MutableList<Content?>?=null

        constructor()
        constructor(text:String?) {
            this.contents=mutableListOf<Content?>(Content(Part(text)))
        }
    }

    class Content {
        var parts:MutableList<Part?>?=null

        constructor()
        constructor(part:Part) {
            this.parts=mutableListOf<Part?>(part)
        }
    }

    class Part {
        var text:String?=null

        constructor()
        constructor(text:String?) {
            this.text=text
        }
    }

    class Response {
        var candidates:MutableList<Candidate?>?=null
    }

    class Candidate {
        var content:Content?=null
    }
}