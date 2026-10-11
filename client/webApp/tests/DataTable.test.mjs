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

test("renders table headers and rows for provided data",async function testDataTableRows(){
    const markup=await renderComponent(
        server,
        "/src/components/DataTable/DataTable.tsx",
        "DataTable",
        {
            columns: [
                {title: "Band",value: item => item.name},
                {title: "Country",value: item => item.country},
            ],
            data: [
                {name: "Nightwish",country: "Finland"},
                {name: "Epica",country: "Netherlands"},
            ],
            emptyText: "Nothing here",
        },
    )

    assert.match(markup,/Band/)
    assert.match(markup,/Country/)
    assert.match(markup,/Nightwish/)
    assert.match(markup,/Epica/)
})

test("renders the empty-state message when there are no rows",async function testDataTableEmptyState(){
    const markup=await renderComponent(
        server,
        "/src/components/DataTable/DataTable.tsx",
        "DataTable",
        {
            columns: [
                {title: "Band",value: item => item.name},
            ],
            data: [],
            emptyText: "No rows available",
        },
    )

    assert.match(markup,/No rows available/)
})
