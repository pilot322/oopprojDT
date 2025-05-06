package managers;

import system.BankSystem;

public abstract class Manager {
    BankSystem systemRef;
    
    Manager(BankSystem systemRef){
        this.systemRef = systemRef;
    }
}
