import React from "react"
import {EmptyState,Table} from "@heroui/react"
import {InboxIcon} from "@heroicons/react/24/solid"

export interface DataTableColumn<T> {
    title: string
    value: (item: T) => React.ReactNode
}

interface DataTableProps<T> {
    columns: readonly DataTableColumn<T>[]
    data: readonly T[]
    emptyText: string
}

export function DataTable<T>({columns,data,emptyText}: DataTableProps<T>){
    return (
        <Table variant={"secondary"}>
            <Table.ScrollContainer
                className={"max-h-96 w-full min-w-0 max-w-full overflow-x-auto overflow-y-auto"}>
                <Table.Content className={"w-max min-w-full whitespace-nowrap"} aria-label={"universal data table"}>
                    <Table.Header className={"sticky top-0 z-10"}>
                        {columns.map((column,index) => (
                            <Table.Column key={index} isRowHeader={index===0}>
                                {column.title}
                            </Table.Column>
                        ))}
                    </Table.Header>
                    <Table.Body
                        renderEmptyState={() => (
                            <EmptyState
                                className="flex min-h-28 flex-col items-center justify-center gap-4 text-center">
                                <InboxIcon className="size-6 text-muted"/>
                                <span className="text-sm text-muted">{emptyText}</span>
                            </EmptyState>
                        )}
                    >
                        {data.map((item,rowIndex) => (
                            <Table.Row key={rowIndex}>
                                {columns.map((column,columnIndex) => (
                                    <Table.Cell key={columnIndex}>
                                        {column.value(item)}
                                    </Table.Cell>
                                ))}
                            </Table.Row>
                        ))}
                    </Table.Body>
                </Table.Content>
            </Table.ScrollContainer>
        </Table>
    )
}