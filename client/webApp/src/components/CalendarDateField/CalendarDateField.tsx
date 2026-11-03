import {Calendar,DateField,DatePicker,DateValue,Description,FieldError,I18nProvider,Label} from "@heroui/react"
import React from "react"
import {CalendarDate,CalendarDateTime,ZonedDateTime} from "@internationalized/date";

//params: isDisabled, minValue, maxValue, classname, name, value, onchange, label, fielderror, description
export function CalendarDateField({
                                      isDisabled,
    isInvalid,
                                      minValue,
                                      maxValue,
                                      className,
                                      name,
                                      value,
                                      onChange,
                                      label,
                                      fieldError,
                                      description
                                  }: {
    isDisabled: boolean,
    isInvalid? : boolean,
    minValue: DateValue | null,
    maxValue: DateValue | null,
    className?: string,
    name?: string,
    value: DateValue | null,
    onChange: (value: CalendarDate | CalendarDateTime | ZonedDateTime | null) => void,
    label: string,
    fieldError: string,
    description: string | null
}){
    return <>
        <I18nProvider locale="en-GB">
            <DatePicker
                isDisabled={isDisabled}
                isRequired
                isInvalid={isInvalid}
                minValue={minValue}
                maxValue={maxValue}
                className={className+" w-72"}
                name={name??"date"}
                value={value}
                onChange={onChange} //NO!
            >
                <Label>{label}</Label>
                <DateField.Group fullWidth>
                    <DateField.Input>{(segment) => <DateField.Segment
                        segment={segment}/>}</DateField.Input>
                    <DateField.Suffix>
                        <DatePicker.Trigger>
                            <DatePicker.TriggerIndicator/>
                        </DatePicker.Trigger>
                    </DateField.Suffix>
                </DateField.Group>
                <FieldError>{fieldError}</FieldError>
                <DatePicker.Popover>
                    <Calendar aria-label="Pick date">
                        <Calendar.Header>
                            <Calendar.YearPickerTrigger>
                                <Calendar.YearPickerTriggerHeading/>
                                <Calendar.YearPickerTriggerIndicator/>
                            </Calendar.YearPickerTrigger>
                            <Calendar.NavButton slot="previous"/>
                            <Calendar.NavButton slot="next"/>
                        </Calendar.Header>
                        <Calendar.Grid>
                            <Calendar.GridHeader>
                                {(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}
                            </Calendar.GridHeader>
                            <Calendar.GridBody>{(date) => <Calendar.Cell
                                date={date}/>}</Calendar.GridBody>
                        </Calendar.Grid>
                        <Calendar.YearPickerGrid>
                            <Calendar.YearPickerGridBody>
                                {({year}) => <Calendar.YearPickerCell year={year}/>}
                            </Calendar.YearPickerGridBody>
                        </Calendar.YearPickerGrid>
                    </Calendar>
                </DatePicker.Popover>
                <Description>{description}</Description>
            </DatePicker>
        </I18nProvider>
    </>
}