export class Account {
    id?: string;
    name?: string;
    surname?: string;
    email?: string;
    language?: string;
    role?: Role;
    authorities?: Authority[];
    avatar?: any;
    token: string;
    fullname?: string;
}

export class Authority {
    authority: Role
}

export enum Role {
    ROLE_ADMIN = "ROLE_ADMIN", ROLE_USER = "ROLE_USER"
}