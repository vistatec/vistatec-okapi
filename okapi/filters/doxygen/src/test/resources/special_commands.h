// Commands from http://www.stack.nl/~dimitri/doxygen/commands.html
// Examples taken from same, except where noted

// \addtogroup //

  /*! \addtogroup mygrp
   *  Additional documentation for group 'mygrp'
   *  @{
   */

  /*!
   *  A function
   */
  void func1()
  {
  }

  /*! Another function */
  void func2()
  {
  }

  /*! @} */

// \class //

/* A dummy class */

class Test
{
};

/*! \class Test class.h "inc/class.h"
 *  \brief This is a test class.
 *
 * Some details about the Test class
 */

// \def //

/*! \file define.h
    \brief testing defines
    
    This is to test the documentation of defines.
*/

/*!
  \def MAX(x,y)
  Computes the maximum of \a x and \a y.
*/

/*! 
   Computes the absolute value of its argument \a x.
*/
#define ABS(x) (((x)>0)?(x):-(x))
#define MAX(x,y) ((x)>(y)?(x):(y))
#define MIN(x,y) ((x)>(y)?(y):(x)) 
        /*!< Computes the minimum of \a x and \a y. */

// \enum //

class Test
{
  public:
    enum TEnum { Val1, Val2 };

    /*! Another enum, with inline docs */
    enum AnotherEnum 
    { 
      V1, /*!< value 1 */
      V2  /*!< value 2 */
    };
};

/*! \class Test
 * The class description.
 */

/*! \enum Test::TEnum
 * A description of the enum type.
 */

/*! \var Test::TEnum Test::Val1
 * The description of the first enum value.
 */

// \example //

/** A Test class.
 *  More details about this class.
 */

class Test
{
  public:
    /** An example member function.
     *  More details about this function.
     */
    void example();
};

void Test::example() {}

/** \example example_test.cpp
 * This is an example of how to use the Test class.
 * More details about this example.
 */

// \file //

/** \file file.h
 * A brief file description.
 * A more elaborated file description.
 */

/**
 * A global integer value.
 * More details about this value.
 */
extern int globalValue;

// \fn //

class Test
{
  public:
    const char *member(char,int) throw(std::out_of_range);
};

const char *Test::member(char c,int n) throw(std::out_of_range) {}

/*! \class Test
 * \brief Test class.
 *
 * Details about Test.
 */

/*! \fn const char *Test::member(char c,int n) 
 *  \brief A member function.
 *  \param c a character.
 *  \param n an integer.
 *  \exception std::out_of_range parameter is out of range.
 *  \return a character pointer.
 */

// \headerfile

/**
  \headerfile test.h "test.h"
  \headerfile test.h ""
  \headerfile "" 

  \headerfile test.h <test.h>
  \headerfile test.h <>
  \headerfile <>
 */

// \mainpage //

/*! \mainpage My Personal Index Page
 *
 * \section intro_sec Introduction
 *
 * This is the introduction.
 *
 * \section install_sec Installation
 *
 * \subsection step1 Step 1: Opening the box
 *
 * etc...
 */

// \overload //

class Test 
{
  public:
    void drawRect(int,int,int,int);
    void drawRect(const Rect &r);
};

void Test::drawRect(int x,int y,int w,int h) {}
void Test::drawRect(const Rect &r) {}

/*! \class Test
 *  \brief A short description.
 *   
 *  More text.
 */

/*! \fn void Test::drawRect(int x,int y,int w,int h)
 * This command draws a rectangle with a left upper corner at ( \a x , \a y ),
 * width \a w and height \a h. 
 */

/*!
 * \overload void Test::drawRect(const Rect &r)
 */

// \page //

/*! \page page1 A documentation page
  \tableofcontents
  Leading text.
  \section sec An example section
  This page contains the subsections \ref subsection1 and \ref subsection2.
  For more info see page \ref page2.
  \subsection subsection1 The first subsection
  Text.
  \subsection subsection2 The second subsection
  More text.
*/

/*! \page page2 Another page
  Even more info.
*/

// \relates //

/*! 
 * A String class.
 */ 
  
class String
{
  friend int strcmp(const String &,const String &);
};

/*! 
 * Compares two strings.
 */

int strcmp(const String &s1,const String &s2)
{
}

/*! \relates String
 * A string debug function.
 */
void stringDebug()
{
}

// \author //

/*! 
 *  \brief     Pretty nice class.
 *  \details   This class is used to demonstrate a number of section commands.
 *  \author    John Doe
 *  \author    Jan Doe
 *  \version   4.1a
 *  \date      1990-2011
 *  \pre       First initialize the system.
 *  \bug       Not all memory is freed when deleting an object of this class.
 *  \warning   Improper use can crash your application
 *  \copyright GNU Public License.
 */
class SomeNiceClass {};

// \cond //

/** An interface */
class Intf
{
  public:
    /** A method */
    virtual void func() = 0;

    /// @cond TEST

    /** A method used for testing */
    virtual void test() = 0;

    /// @endcond
};

/// @cond DEV
/*
 *  The implementation of the interface
 */
class Implementation : public Intf
{
  public:
    void func();

    /// @cond TEST
    void test();
    /// @endcond

    /// @cond
    /** This method is obsolete and does
     *  not show up in the documentation.
     */
    void obsolete();
    /// @endcond
};

/// @endcond

// \if //

  /*! Unconditionally shown documentation.
   *  \if Cond1
   *    Only included if Cond1 is set.
   *  \endif
   *  \if Cond2
   *    Only included if Cond2 is set.
   *    \if Cond3
   *      Only included if Cond2 and Cond3 are set.
   *    \endif
   *    More text.
   *  \endif
   *  Unconditional text.
   */

/*! \english
 *  This is English.
 *  \endenglish
 *  \dutch
 *  Dit is Nederlands.
 *  \enddutch
 */
class Example
{
};

// \par //

/*! \class Test
 * Normal text.
 *
 * \par User defined paragraph:
 * Contents of the paragraph.
 *
 * \par
 * New paragraph under the same heading.
 *
 * \note
 * This note consists of two paragraphs.
 * This is the first paragraph.
 *
 * \par
 * And this is the second paragraph.
 *
 * More normal text. 
 */
  
class Test {};

// \param //

/*!
Copies bytes from a source memory area to a destination memory area,
where both areas may not overlap.
@param[out] dest The memory area to copy to.
@param[in] src The memory area to copy from.
@param[in] n The number of bytes to copy
*/
void memcpy(void *dest, const void *src, size_t n);

/** Sets the position.
@param x,y,z Coordinates of the position in 3D space.
*/
void setPosition(double x,double y,double z,double t)
{
}

// \xrefitem //

/**
 \xrefitem todo "Todo" "Todo List" 
*/

// \link & \endlink //
// from http://www.stack.nl/~dimitri/doxygen/autolink.html

/*
A link to a variable \link #var using another text\endlink as a link.
*/

// \subpage //

/*! \mainpage A simple manual

Some general info.

This manual is divided in the following sections:
- \subpage intro
- \subpage advanced "Advanced usage"
*/

//-----------------------------------------------------------

/*! \page intro Introduction
This page introduces the user to the topic.
Now you can proceed to the \ref advanced "advanced section".
*/

//-----------------------------------------------------------

/*! \page advanced Advanced Usage
This page is for advanced users.
Make sure you have first read \ref intro "the introduction".
*/

// \dontinclude //

/*! A test class. */

class Test
{
  public:
    /// a member function
    void example();
};

/*! \page example
 *  \dontinclude example_test.cpp
 *  Our main function starts like this:
 *  \skip main
 *  \until {
 *  First we create a object \c t of the Test class.
 *  \skipline Test
 *  Then we call the example member function 
 *  \line example
 *  After that our little test routine ends.
 *  \line }
 */

// \skipline //

/**
\skipline pattern
*/

// \snippet //

/**
\snippet snippets/example.cpp Adding a resource
*/

// \a //

/**
 ... the \a x and \a y coordinates are used to ...
*/

// \arg //

/**
  \arg \c AlignLeft left alignment.
  \arg \c AlignCenter center alignment.
  \arg \c AlignRight right alignment

  No other types of alignment are supported.
 */

// \c //

/**
     ... This function returns \c void and not \c int ...
 */

// \code //

/**
  \code{.py}
  class Python:
     pass
  \endcode

  \code{.cpp}
  class Cpp {};
  \endcode
 */

// \copydoc //

  /*! @copydoc MyClass::myfunction()
   *  More documentation.
   */

  //! @copydoc MyClass::myfunction(type1,type2)

// \dot //

/*! class B */
class B {};
/*! class C */
class C {};
/*! \mainpage
  
	Class relations expressed via an inline dot graph:
	\dot
	digraph example {
	node [shape=record, fontname=Helvetica, fontsize=10];
		b [ label="class B" URL="\ref B"];
		c [ label="class C" URL="\ref C"];
		b -> c [ arrowhead="open", style="dashed" ];
	}
	\enddot
	Note that the classes in the above graph are clickable
	(in the HTML output).
*/

// \msc //

/** Sender class. Can be used to send a command to the server.
	The receiver will acknowledge the command by calling Ack().
	\msc
	  Sender,Receiver;
	  Sender->Receiver [label="Command()", URL="\ref Receiver::Command()"];
	  Sender<-Receiver [label="Ack()", URL="\ref Ack()", ID="1"];
	\endmsc
*/
class Sender
{
  public:
	/** Acknowledgement from server */
	void Ack(bool ok);
};
/** Receiver class. Can be used to receive and execute commands.
	After execution of a command, the receiver will send an acknowledgement
	\msc
	  Receiver,Sender;
	  Receiver<-Sender [label="Command()", URL="\ref Command()"];
	  Receiver->Sender [label="Ack()", URL="\ref Sender::Ack()", ID="1"];
	\endmsc
 */
class Receiver
{
  public:
	/** Executable a command on the server */
	void Command(int commandId);
};

// \e //

/**
  ... this is a \e really good example ...
 */

// \em //

/**
  ... this is a \em really good example ...
 */

// \image //

  /*! Here is a snapshot of my new application:
   *  \image html application.jpg
   *  \image latex application.eps "My application" width=10cm
   */

// \li //

/**
  \li \c AlignLeft left alignment.
  \li \c AlignCenter center alignment.
  \li \c AlignRight right alignment

  No other types of alignment are supported.
*/

// \p //

/**
  ... the \p x and \p y coordinates are used to ...
 */

// \~[LanguageId] //

/*! \~english This is english \~dutch Dit is Nederlands \~german Dieses ist
    deutsch. \~ output for all languages.
 */
