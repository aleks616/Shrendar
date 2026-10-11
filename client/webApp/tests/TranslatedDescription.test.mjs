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

test("renders the original description and a translate action",async function testTranslatedDescriptionMarkup(){
    const markup=await renderComponent(
        server,
        "/src/components/TranslatedDescription/TranslatedDescription.tsx",
        "TranslatedDescription",
        {
            description: "Original description",
            translateDescription: () => {},
        },
    )

    assert.match(markup,/Original description/)
    assert.match(markup,/<button/)
})
