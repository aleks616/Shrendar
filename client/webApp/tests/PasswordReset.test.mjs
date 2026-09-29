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

test("renders the request-password-reset form disabled while the account field is empty",async function testRendersRequestPasswordResetForm(){
    const markup=await renderComponent(
        server,
        "/src/components/RequestPasswordReset/RequestPasswordReset.tsx",
        "RequestPasswordReset",
    )

    assert.match(markup,/Forgot password\?/)
    assert.match(markup,/Login or e-mail address/)
    assert.match(markup,/Reset password/)
    assert.match(markup,/<button\b[^>]*disabled/)
})

test("renders the create-password fields and disables submission while empty",async function testRendersCreatePasswordForm(){
    const markup=await renderComponent(
        server,
        "/src/components/CreatePassword/CreatePassword.tsx",
        "CreatePassword",
    )

    assert.match(markup,/Create new password/)
    assert.match(markup,/Password/)
    assert.match(markup,/Repeat password/)
    assert.match(markup,/Change password/)
    assert.match(markup,/<button\b[^>]*disabled/)
})