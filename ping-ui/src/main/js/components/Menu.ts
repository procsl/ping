
interface Menu {

    getName(): string;

    getOrder(): number;

    getSubMenus(): Menu[];

    getTargetUrl(): string;

    getIcon(): string;
}