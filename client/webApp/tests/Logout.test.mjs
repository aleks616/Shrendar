import assert from "node:assert/strict"
import {after,before,test} from "node:test"
import {createComponentTestServer} from "./test-utils.mjs"

let server

before(async () => {
    server=await createComponentTestServer()
})

after(async () => {
    await server.close()
})

test("returns no_token when no account is signed in",async function testLogoutWithoutToken(){
    const {logout}=await server.ssrLoadModule("/src/components/Logout/Logout.tsx")

    assert.equal(await logout(),"no_token")
})
