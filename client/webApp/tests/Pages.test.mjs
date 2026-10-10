import assert from "node:assert/strict"
import {after,before,test} from "node:test"
import {createComponentTestServer,renderComponent} from "./test-utils.mjs"

let server

before(async () => {
    server=await createComponentTestServer()
})

after(async () => {
    await server.close()
})

const pages=[
    ["/src/pages/Home/index.tsx","Home","Home page"],
    ["/src/pages/Login/index.tsx","Login","Sign in"],
    ["/src/pages/Register/index.tsx","Register","Create account"],
    ["/src/pages/ForgotPassword/index.tsx","ForgotPassword","Forgot password?"],
    ["/src/pages/SetPassword/index.tsx","SetPassword","Create new password"],
    ["/src/pages/ProfilePage/index.tsx","ProfilePage",null],
    ["/src/pages/UserSettings/index.tsx","UserSettings",null],
    ["/src/pages/BandWikiPage/index.tsx","BandWikiPage",null],
    ["/src/pages/ArtistWikiPage/index.tsx","ArtistWikiPage",null],
    ["/src/pages/AlbumWikiPage/index.tsx","AlbumWikiPage",null],
    ["/src/pages/EventWikiPage/index.tsx","EventWikiPage",null],
    ["/src/pages/NotFound/NotFound.tsx","NotFound","Not found 404"],
]

for(const [modulePath,exportName,expectedText] of pages){
    test(`renders ${exportName} independently`,async function testPage(){
        const markup=await renderComponent(server,modulePath,exportName)

        assert.ok(markup.length>0)
        if(expectedText!==null){
            assert.match(markup,new RegExp(expectedText.replace(/[?]/g,"\\$&")))
        }
    })
}
