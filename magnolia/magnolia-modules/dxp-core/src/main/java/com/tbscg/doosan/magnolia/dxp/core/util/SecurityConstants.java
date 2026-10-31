package com.tbscg.doosan.magnolia.dxp.core.util;

public class SecurityConstants {

    public static final String DXP_GROUP_PREFIX = "dxp-";

    public static final class UserTypes {

        public static final class ContentApprover {

            public static final class Group {
                public static final String NAME = DXP_GROUP_PREFIX + "content-approvers";
            }

            public static final class Role {
                public static final String NAME = "content-approver";
            }
        }

        public static final class ContentAuthor {

            public static final class Group {
                public static final String NAME = DXP_GROUP_PREFIX + "content-authors";
            }

            public static final class Role {
                public static final String NAME = "content-author";
            }
        }

        public static final class Admin {

            public static final class Group {
                public static final String NAME = DXP_GROUP_PREFIX + "admins";
            }
        }
    }
}
