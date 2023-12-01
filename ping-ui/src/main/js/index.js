import React from "react";
import { Menu } from "@headlessui/react";
console.log("hello world");
function MyDropdown() {
    return (React.createElement(Menu, null,
        React.createElement(Menu.Button, null, "More"),
        React.createElement(Menu.Items, null,
            React.createElement(Menu.Item, null, "Account settings"),
            React.createElement(Menu.Item, null, "Documentation"),
            React.createElement(Menu.Item, { disabled: true }, "Invite a friend (coming soon!)"))));
}
