import { useState } from 'react';
import AuthContext from './AuthContext';

const AuthProvider = ({ children }) => {
  // here 'children' (can be named something else) is just a generic name
  // everything that is coming pass them forward
  // 'children' = everything wrapped inside this component
  // React automatically passes it

  const [user, setUser] = useState(null);

  const isAuthenticated = user !== null;

  const login = (userDate) => {
    setUser(userDate);
  };

  const logout = () => {
    setUser(null);
  };

  const signup = (userDate) => {
    setUser(userDate);
  };

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated, login, logout, signup }}
    >
      {children}
    </AuthContext.Provider>
  );

  // AuthContext.Provider   = makes data available globally
  // value                  = is what you want to share
  // {children}             = Render whatever components were wrapped inside this provider.”
};

export default AuthProvider;

/*
    <AuthContext.Provider>
        <App />     // HERE, <App /> become children
    </AuthContext.Provider>
*/
