// Copyright MyCompany 2012

#include <myheader>

/// \class MyClass
/// \brief Brief description.
///
/// A more detailed class description
/// spanning multiple lines.
/// 
class MyClass {
  public:
  
    /// constructor; initialize the list to be empty
    /// - This is a \b list.
    /// - This item spans multiple
    /// lines.
    ///   -# This item has an \invalid command in it.
    ///
    /// Post-list paragraph.
    ///
    MyClass();

    /// \param a This paragraph has \param b many different \param c text
    /// units \param d all smashed together.
    void MyFunction(int i);
    
    /// Here's some embedded sample code that shouldn't be extracted:
    /// \code
    ///     jimmy.crack('corn') && me['care'] == false;
    /// \endcode
    /// 
    /// Follow it up with another paragraph for good measure.
    void Print(ostream &output) const;

    // Not a Doxygen comment. This should not be extracted.
    
    /// Paragraph one
    /// 
    /// Paragraph two separated only by a blank line.
  private:
    static const int SIZE = 10;      ///< initial size of the array
    int *Items;                      ///< Items will point to the dynamically allocated array
    int numItems;                    ///< number of items currently in the list
    int arraySize;                   ///< the current size of the array

// /**
//  * Here is a commented-out JavaDoc comment.
//  * This shouldn't be extracted either.
//  */
//
// /// One more, for good measure!
// /// 

    string s = "/** Here is a comment embedded in a comment. */"
    s += '/// These should not be \'extracted\' either!'
    s += '/// Ya dig?'
};