import assert from "node:assert/strict"
import {after,before,beforeEach,test} from "node:test"
import {createComponentTestServer,renderComponent,resetLocalStorage} from "./test-utils.mjs"

let server

before(async () => {
    server=await createComponentTestServer()
})

beforeEach(() => {
    resetLocalStorage()
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
    ["/src/pages/ProfilePage/index.tsx","ProfilePage","Sign Up"],
    ["/src/pages/UserSettings/index.tsx","UserSettings","Save changes"],
    ["/src/pages/BandWikiPage/index.tsx","BandWikiPage","Sign Up"],
    ["/src/pages/ArtistWikiPage/index.tsx","ArtistWikiPage","Sign Up"],
    ["/src/pages/AlbumWikiPage/index.tsx","AlbumWikiPage","Sign Up"],
    ["/src/pages/EventWikiPage/index.tsx","EventWikiPage","Sign Up"],
    ["/src/pages/NotFound/NotFound.tsx","NotFound","Not found 404"],
]

for(const [modulePath,exportName,expectedText] of pages){
    test(`renders ${exportName} independently`,async function testPage(){
        const markup=await renderComponent(server,modulePath,exportName)

        assert.ok(markup.length>0)
        assert.match(markup,new RegExp(expectedText.replace(/[?]/g,"\\$&")))
    })
}
