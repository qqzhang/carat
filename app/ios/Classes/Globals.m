//
//  Globals.m
//  Carat
//
//  Singleton globals, courtesy http://www.eschatologist.net/blog/?p=178 which
//  suggests avoiding the complicated stuff in Cocoa Fundamentals Guide.
//
//  Created by Anand Padmanabha Iyer on 11/10/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Globals.h"
#import "Utilities.h"

@implementation Globals

@synthesize myUUID;
@synthesize defaults;

static id instance = nil;
//static NSUserDefaults* defaults = nil;

+ (void) initialize {
    if (self == [Globals class]) {
        instance = [[self alloc] init];
        [instance getUUIDFromNSUserDefaults];
    }
}

+ (id) instance {
    return instance;
}

- (void) getUUIDFromNSUserDefaults
{
    self.defaults = [NSUserDefaults standardUserDefaults];
    self.myUUID = [[self defaults] objectForKey:@"CaratUUID"];
}

//
// Generate a new UUID using CFUUIDCreateString. Save the generated ID in 
// NSUserDefaults.
//
- (NSString *) generateUUID 
{
    CFUUIDRef uuidObject = CFUUIDCreate(kCFAllocatorDefault);
    NSString *uuidStr = [(NSString *)CFUUIDCreateString(kCFAllocatorDefault, uuidObject) autorelease];
    CFRelease(uuidObject);
    DLog(@"%s Generated new UUID: %@", __PRETTY_FUNCTION__, uuidStr);
    [defaults setObject:uuidStr forKey:@"CaratUUID"];
    [defaults synchronize];
    return uuidStr;
}

//
// Get the Unique ID for the device. 
// 
- (NSString *) getUUID 
{    
    if (self.myUUID == nil) 
    {
        self.myUUID = [self generateUUID];
    }
    
    return self.myUUID;
}

//
// Convert local datetime to UTC.
// From: http://stackoverflow.com/questions/1081647/how-to-convert-time-to-the-timezone-of-the-iphone-device
//
- (NSDate *) utcDateTime {
    NSDate* sourceDate = [NSDate date];
    NSTimeZone* sourceTimeZone = [NSTimeZone systemTimeZone];
    NSTimeZone* destinationTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"UTC"];
    NSInteger sourceGMTOffset = [sourceTimeZone secondsFromGMTForDate:sourceDate];
    NSInteger destinationGMTOffset = [destinationTimeZone secondsFromGMTForDate:sourceDate];
    NSTimeInterval interval = destinationGMTOffset - sourceGMTOffset;
    NSDate* destinationDate = [[[NSDate alloc] initWithTimeInterval:interval sinceDate:sourceDate] autorelease];
    return destinationDate;
}

//
// Return seconds since epoch.
//
- (double) utcSecondsSinceEpoch {
    //NSDate* dateTimeInUTC = [self utcDateTime];
    NSDate* dateTimeInUTC = [NSDate date];    // NSDate has no concept of timezone, so it always gives UTC.
    return (double) [dateTimeInUTC timeIntervalSince1970];
}

//
//  Update user consent value.
//
- (void) userHasConsented {
    [defaults setBool:YES forKey:@"CaratUserConsented"];
    [defaults synchronize];
}

//
//  Get the value of user consent.
//
- (BOOL) hasUserConsented { 
    if (self.defaults != nil)
        return [[self defaults] boolForKey:@"CaratUserConsented"];
    return NO;
}

//
// Save distance traveled.
//
- (void) setDistanceTraveled:(double)distance 
{
    [defaults setDouble:distance forKey:@"CaratDistanceTraveled"];
    [defaults synchronize];
}

//
// Get the value of distance traveled.
//
- (double) getDistanceTraveled
{
    if (self.defaults != nil)
        return [[self defaults] doubleForKey:@"CaratDistanceTraveled"];
    return 0.0;
}

- (void) dealloc
{
    [myUUID release];
    [defaults release];
    [super dealloc];
}

@end
