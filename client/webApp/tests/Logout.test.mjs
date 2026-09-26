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

test("renders a localized logout button",async function testRendersLocalizedLogoutButton(){
    const markup=await renderComponent(
        server,
        "/src/components/Logout/Logout.tsx",
        "default",
    )

    assert.match(markup,/<button\b/)
    assert.match(markup,/Log out/)
})
