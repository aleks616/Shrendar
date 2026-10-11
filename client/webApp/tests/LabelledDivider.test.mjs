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

test("renders its label between two accessible separators",async function testRendersLabelBetweenSeparators(){
    const markup=await renderComponent(
        server,
        "/src/components/LabelledDivider/LabelledDivider.tsx",
        "default",
        {text: "Continue with"},
    )

    assert.match(markup,/Continue with/)
    assert.equal((markup.match(/role="separator"|<hr\b/g)??[]).length,4)
})
