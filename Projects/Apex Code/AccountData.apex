public class AccountData 
{
    public void NNN()
    {
        sObject obj=new Account(Name='Test');
        Account acc =  (Account)obj;
        System.debug('Account ='+acc);
    }
}