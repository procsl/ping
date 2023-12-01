import {Menu} from "@headlessui/react";

console.log("hello world");

function MyDropdown() {
    return (
        <Menu>
            <Menu.Button>More</Menu.Button>
            <Menu.Items>
                <Menu.Item>
                    Account settings
                </Menu.Item>
                <Menu.Item>
                    Documentation
                </Menu.Item>
                <Menu.Item disabled>
                    Invite a friend (coming soon!)
                </Menu.Item>
            </Menu.Items>
        </Menu>
    )
}