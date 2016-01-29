package com.dw.user;

public class UserCreateStatus
{
        public static final int Success = 1 ;
        public static final int InvalidUserName= 2 ;
        public static final int InvalidPassword= 3 ;
        public static final int InvalidQuestion= 4 ;
        public static final int InvalidAnswer= 5 ;
        public static final int InvalidEmail= 6 ;
        public static final int DuplicateUserName= 7 ;
        public static final int DuplicateEmail= 8 ;
        public static final int UserRejected= 9 ;
        public static final int InvalidProviderUserKey= 10 ;
        public static final int DuplicateProviderUserKey= 11 ;
        public static final int ProviderError= 12 ;
        
        private int st_val = -1 ;
        
        public UserCreateStatus()
        {
        	
        }
        
        public int getStatusValue()
        {
        	return st_val ;
        }
        
        void setStatusValue(int stv)
        {
        	st_val = stv ;
        }
}
