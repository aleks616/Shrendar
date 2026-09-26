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

test("renders the registration fields and disables submission while empty",async function testRendersRegistrationFormInitiallyDisabled(){
    const markup=await renderComponent(
        server,
        "/src/components/Register/Register.tsx",
        "Register",
    )

    assert.match(markup,/Create account/)
    assert.match(markup,/E-mail address/)
    assert.match(markup,/Login/)
    assert.match(markup,/Repeat password/)
    assert.match(markup,/Sign Up/)
    assert.match(markup,/<button\b[^>]*disabled/)
})
