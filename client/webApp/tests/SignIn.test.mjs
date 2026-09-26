import assert from "node:assert/strict"
import {after,before,test} from "node:test"
import {createComponentTestServer,renderComponent} from "./test-utils.mjs"

let server

before(async () => {
    server= await createComponentTestServer()
})

after(async () => {
    await server.close()
})

test("renders the sign-in fields and disables submission while empty",async function testRendersSignInFormInitiallyDisabled(){
    const markup=await renderComponent(
        server,
        "/src/components/SignIn/SignIn.tsx",
        "SignIn",
    )

    assert.match(markup,/Sign in/)
    assert.match(markup,/Login or e-mail address/)
    assert.match(markup,/Password/)
    assert.match(markup,/type="password"/)
    assert.match(markup,/<button\b[^>]*disabled/)
})
