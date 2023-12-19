type MenuProp = {
    name: string,
    url: string,
    icon: string,
    order: number,
    target: string,
    subMenu: Menu
}

export class SimpleMenu implements Menu {


    getName(): string {
        return "";
    }

    getOrder(): number {
        return 0;
    }

    getSubMenus(): Menu[] {
        return [];
    }

    getTargetUrl(): string {
        return "";
    }

    getIcon(): string {
        return "";
    }

}

