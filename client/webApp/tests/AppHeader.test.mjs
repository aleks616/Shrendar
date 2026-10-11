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

test("renders register and sign-in actions for guests",async function testGuestHeader(){
    const markup=await renderComponent(
        server,
        "/src/components/AppHeader/AppHeader.tsx",
        "AppHeader",
    )

    assert.match(markup,/Sign Up/)
    assert.match(markup,/Sign in/)
    assert.doesNotMatch(markup,/Log out/)
})

test("renders authenticated header chrome when a token and login are present",async function testAuthenticatedHeader(){
    resetLocalStorage({token: "token-123",login: "alice"})

    const markup=await renderComponent(
        server,
        "/src/components/AppHeader/AppHeader.tsx",
        "AppHeader",
    )

    assert.match(markup,/aria-haspopup="true"/)
    assert.match(markup,/badge__label"[^>]*>[0-9]</)
    assert.doesNotMatch(markup,/Sign Up/)
    assert.doesNotMatch(markup,/Sign in/)
})
