import assert from "node:assert/strict"
import {after,before,test} from "node:test"
import {createComponentTestServer,renderComponent,resetLocalStorage} from "./test-utils.mjs"

let server

before(async () => {
    server=await createComponentTestServer()
})

after(async () => {
    await server.close()
})

test("renders settings fields and primary actions without loaded user data",async function testSettingsStaticMarkup(){
    resetLocalStorage()

    const markup=await renderComponent(
        server,
        "/src/components/Settings/Settings.tsx",
        "Settings",
    )

    assert.match(markup,/Username/)
    assert.match(markup,/E-mail address/)
    assert.match(markup,/Birthdate/)
    assert.match(markup,/Save changes/)
    assert.match(markup,/Change password/)
    assert.match(markup,/Delete account/)
})
