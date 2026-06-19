# UI 2

Inspired by SwiftUI driven by JSON layout system with a goal of providing declarative UI for our mods.

## JSON driven

Component based nested JSON layout system.

## Layout engine

Stack based layout engine via VStack and HStack components with overflow / scrolling support.

Space negotiation and alignment model via a 3 pass cycle
- Parent (Root View or Containers) which defines the "space" available for children
- Children which define their "space" requirements
- Positioning pass which calculates the final position of each child based on the available space and their requirements.

### Modifiers

SwiftUI defines the concept of "modifiers" which are used to modify the properties of a view

Proposed modifiers for UI 2:

```json
{
  "type": "text",
  "text": "This is a test layout",
  "modifiers": [
    {
      "type": "font",
      "value": "bold"
    },
    {
      "type": "color",
      "value": "#FF0000"
    }
  ]
}
```

## State management

// TODO: Add state management section

## Actions

Actions are defined via the "action": "namespace:action_name" property which are then registered upon initialization of the layout. These actions are defined by a `Consumer<UIActionContext>` which is called when the action is triggered.

## Variables

Variable parsing via a `$` prefix

```json
{
  "name": "Test Layout",
  "elements": [
    {
      "type": "vstack",
      "elements": [
        {
          "type": "text",
          "text": "This is a test layout"
        },
        {
          "type": "text",
          "translation": "ftblibrary.test_translation",
          "with": [
            "argument1",
            "$test_variable"
          ]
        },
        {
          "type": "button",
          "text": "Click me",
          "action": "ftblibrary:test_action"
        }
      ]
    }
  ]
}
```
